#include <jni.h>
#include <time.h>
#include <android/log.h>
#include <android/bitmap.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <stdbool.h>

#include "fitz.h"
#include "mupdf.h"
#include "mupdf-internal.h"


#define LOG_TAG "libmupdf"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

/* Set to 1 to enable debug log traces. */
#define DEBUG 0

/* Enable to log rendering times (render each frame 100 times and time) */
#undef TIME_DISPLAY_LIST

#define MAX_SEARCH_HITS (10)

#define MAX_SEARCH_ARRAYS 500
#define MAX_SEARCH_SAVE 128
#define MAX_SEARCH_STRING 100

/* Globals */
fz_colorspace *colorspace;
fz_document *doc;
int pagenum = 1;
int resolution = 160;
float pageWidth = 100;
float pageHeight = 100;
fz_display_list *currentPageList;
fz_rect currentMediabox;
fz_context *ctx;
int currentPageNumber = -1;
fz_page *currentPage = NULL;
fz_bbox *hit_bbox = NULL;
int i4totalpagenum = 0;
float g_xscale = 0;
float g_yscale = 0;
fz_bbox g_wb_rect;
bool g_fgNewline = false;

#if SUPPORT_BOOKMARK
char g_chBookmarkStr[128] = {0};
#endif

JNIEXPORT int JNICALL
Java_com_yt_reader_format_pdf_MuPDFCore_openFile(JNIEnv * env, jobject thiz, jstring jfilename)
{
	PDFLOGI("[mupdf.c] Java_com_yt_reader_format_pdf_MuPDFCore_openFile start \n");

	const char *filename;
	int pages = 0;
	int result = 0;

	filename = (*env)->GetStringUTFChars(env, jfilename, NULL);
	if (filename == NULL)
	{
		LOGE("Failed to get filename");
		return 0;
	}

	/* 128 MB store for low memory devices. Tweak as necessary. */
	ctx = fz_new_context(NULL, NULL, 128 << 20);
	if (!ctx)
	{
		LOGE("Failed to initialise context");
		return 0;
	}

	doc = NULL;
	fz_try(ctx)
	{
		colorspace = fz_device_rgb;

		LOGE("Opening document...");
		fz_try(ctx)
		{
			doc = fz_open_document(ctx, (char *)filename);
		}
		fz_catch(ctx)
		{
			fz_throw(ctx, "Cannot open document: '%s'\n", filename);
		}
#if SUPPORT_BOOKMARK

		// init g_chBookmarkStr to 0
		int i4i = 0;
		for(i4i = 0; i4i < 128; i4i++)
		{
			g_chBookmarkStr[i4i] = '\0';
		}
#endif
		LOGE("Open document is done!");
		result = 1;
	}
	fz_catch(ctx)
	{
		LOGE("Failed: %s", ctx->error->message);
		fz_close_document(doc);
		doc = NULL;
		fz_free_context(ctx);
		ctx = NULL;
	}

	(*env)->ReleaseStringUTFChars(env, jfilename, filename);

	return result;
}

JNIEXPORT int JNICALL
Java_com_yt_reader_format_pdf_MuPDFCore_countPagesInternal(JNIEnv *env, jobject thiz)
{
	i4totalpagenum = fz_count_pages(doc);
	LOGE("Total page number is %d", i4totalpagenum);
	return i4totalpagenum;
}

JNIEXPORT void JNICALL
Java_com_yt_reader_format_pdf_MuPDFCore_gotoPageInternal(JNIEnv *env, jobject thiz, int page)
{
	//PDFLOGI("[mupdf.c] Java_com_yt_reader_format_pdf_MuPDFCore_gotoPageInternal start, pageNumber is %d \n", page);

	float zoom;
	fz_matrix ctm;
	fz_bbox bbox;
	fz_device *dev = NULL;

	fz_var(dev);

	if (currentPage != NULL && page != currentPageNumber)
	{
		fz_free_page(doc, currentPage);
		currentPage = NULL;
	}

	/* In the event of an error, ensure we give a non-empty page */
	pageWidth = 100;
	pageHeight = 100;

	currentPageNumber = page;
	//PDFLOGI("[mupdf.c] Goto page %d...", page);
	fz_try(ctx)
	{
		if (currentPageList != NULL)
		{
			fz_free_display_list(ctx, currentPageList);
			currentPageList = NULL;
		}
		pagenum = page;
		currentPage = fz_load_page(doc, pagenum);
		zoom = resolution / 72;
		currentMediabox = fz_bound_page(doc, currentPage);
		ctm = fz_scale(zoom, zoom);
		bbox = fz_round_rect(fz_transform_rect(ctm, currentMediabox));
		pageWidth = bbox.x1-bbox.x0;
		pageHeight = bbox.y1-bbox.y0;
	}
	fz_catch(ctx)
	{
		currentPageNumber = page;
		LOGE("cannot make displaylist from page %d", pagenum);
	}
	fz_free_device(dev);
	dev = NULL;
}

JNIEXPORT float JNICALL
Java_com_yt_reader_format_pdf_MuPDFCore_getPageWidth(JNIEnv *env, jobject thiz)
{
	LOGE("GetPageWidth, PageWidth=%g", pageWidth);
	return pageWidth;
}

JNIEXPORT float JNICALL
Java_com_yt_reader_format_pdf_MuPDFCore_getPageHeight(JNIEnv *env, jobject thiz)
{
	LOGE("GetPageHeight, PageHeight=%g", pageHeight);
	return pageHeight;
}

JNIEXPORT jboolean JNICALL
Java_com_yt_reader_format_pdf_MuPDFCore_drawPage(JNIEnv *env, jobject thiz, jobject bitmap,
		int pageW, int pageH, int patchX, int patchY, int patchW, int patchH)
{
	//PDFLOGI("[mupdf.c] Java_com_yt_reader_format_pdf_MuPDFCore_drawPage start \n");
	//LOGE("Draw Page");

	AndroidBitmapInfo info;
	void *pixels;
	int ret;
	fz_device *dev = NULL;
	float zoom;
	fz_matrix ctm;
	fz_bbox bbox;
	fz_pixmap *pix = NULL;
	float xscale, yscale;
	fz_bbox rect;

	fz_var(pix);
	fz_var(dev);
	
#if SUPPORT_BOOKMARK
	// init g_chBookmarkStr to 0
	int i4i = 0;
	for(i4i = 0; i4i < 128; i4i++)
	{
		g_chBookmarkStr[i4i] = '\0';
	}
#endif

	//PDFLOGI("In native method\n");
	if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
		return 0;
	}

	//PDFLOGI("Checking format\n");
	if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
		LOGE("Bitmap format is not RGBA_8888 !");
		return 0;
	}

	//PDFLOGI("locking pixels\n");
	if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
		return 0;
	}

	/* Call mupdf to render display list to screen */
	PDFLOGI("Rendering page=%dx%d patch=[%d,%d,%d,%d]", pageW, pageH, patchX, patchY, patchW, patchH);

	fz_try(ctx)
	{
		if (currentPageList == NULL)
		{
			/* Render to list */
			currentPageList = fz_new_display_list(ctx);
			dev = fz_new_list_device(ctx, currentPageList);
			fz_run_page(doc, currentPage, dev, fz_identity, NULL);
		}
		rect.x0 = patchX;
		rect.y0 = patchY;
		rect.x1 = patchX + patchW;
		rect.y1 = patchY + patchH;

		g_wb_rect.x0 = rect.x0;
		g_wb_rect.y0 = rect.y0;
		g_wb_rect.x1 = rect.x1;
		g_wb_rect.y1 = rect.y1;

#if 0
		// for test
		PDFLOGI("[mupdf.c] g_wb_rect [x0= %d, y0= %d, x1= %d, y1= %d] \n", g_wb_rect.x0, g_wb_rect.y0, g_wb_rect.x1, g_wb_rect.y1);
		// end test
#endif
		
		pix = fz_new_pixmap_with_bbox_and_data(ctx, colorspace, rect, pixels);
		if (currentPageList == NULL)
		{
			fz_clear_pixmap_with_value(ctx, pix, 0xd0);
			break;
		}
		fz_clear_pixmap_with_value(ctx, pix, 0xff);

		zoom = resolution / 72;
		ctm = fz_scale(zoom, zoom);
		bbox = fz_round_rect(fz_transform_rect(ctm,currentMediabox));

#if 0
		// for test
		PDFLOGI("[mupdf.c] (1)bbox [x0= %d, y0= %d, x1= %d, y1= %d] \n", bbox.x0, bbox.y0, bbox.x1, bbox.y1);
		// end test
#endif
		
		/* Now, adjust ctm so that it would give the correct page width
		 * heights. */
		xscale = (float)pageW/(float)(bbox.x1-bbox.x0);
		yscale = (float)pageH/(float)(bbox.y1-bbox.y0);

		g_xscale = xscale;
		g_yscale = yscale;
		//PDFLOGI("[mupdf.c] g_xscale = %7.2f, g_yscale = %7.2f \n", g_xscale, g_yscale);
		
		ctm = fz_concat(ctm, fz_scale(xscale, yscale));
		bbox = fz_round_rect(fz_transform_rect(ctm,currentMediabox));

#if 0
		// for test
		PDFLOGI("[mupdf.c] (2)bbox [x0= %d, y0= %d, x1= %d, y1= %d] \n", bbox.x0, bbox.y0, bbox.x1, bbox.y1);
		// end test
#endif

		dev = fz_new_draw_device(ctx, pix);

//#define TIME_DISPLAY_LIST
		
#ifdef TIME_DISPLAY_LIST
		{
			clock_t time;
			int i;

			LOGE("Executing display list");
			time = clock();
			for (i=0; i<100;i++) {
#endif
				fz_run_display_list(currentPageList, dev, ctm, bbox, NULL);
#ifdef TIME_DISPLAY_LIST
			}
			time = clock() - time;
			LOGE("100 renders in %d (%d per sec)", time, CLOCKS_PER_SEC);
		}
#endif
		fz_free_device(dev);
		dev = NULL;
		fz_drop_pixmap(ctx, pix);
		LOGE("Draw page is done");
	}
	fz_catch(ctx)
	{
		fz_free_device(dev);
		LOGE("Render failed");
	}

	AndroidBitmap_unlockPixels(env, bitmap);

	return 1;
}

static fz_text_char textcharat(fz_text_page *page, int idx)
{
	static fz_text_char emptychar = { {0,0,0,0}, ' ' };
	static fz_text_char newlinechar = { {0,0,0,0}, 10};
	
	fz_text_block *block;
	fz_text_line *line;
	fz_text_span *span;
	int ofs = 0;
	for (block = page->blocks; block < page->blocks + page->len; block++)
	{
		for (line = block->lines; line < block->lines + block->len; line++)
		{
			for (span = line->spans; span < line->spans + line->len; span++)
			{
				if (idx < ofs + span->len)
				{
					return span->text[idx - ofs];
				}
				
				/* pseudo-newline */
				if (span + 1 == line->spans + line->len)
				{
					if (idx == ofs + span->len)
					{
						return newlinechar;
					}
					ofs++;
				}
				ofs += span->len;
			}
		}
	}
	return emptychar;
}

static fz_text_char textcharatEx(fz_text_page *page, int idx)
{
	static fz_text_char emptychar = { {0,0,0,0}, ' ' };
	static fz_text_char newlinechar = { {0,0,0,0}, 10};

	fz_text_block *block;
	fz_text_line *line;
	fz_text_span *span;
	int ofs = 0;
	for (block = page->blocks; block < page->blocks + page->len; block++)
	{
		for (line = block->lines; line < block->lines + block->len; line++)
		{
			for (span = line->spans; span < line->spans + line->len; span++)
			{
				if (idx < ofs + span->len)
				{
					//PDFLOGI("[mupdf.c] textcharatEx,  c = %d \n", span->text[idx - ofs].c);
					return span->text[idx - ofs];
				}
				
				/* pseudo-newline */
				if (span + 1 == line->spans + line->len)
				{
					if (idx == ofs + span->len)
					{
						g_fgNewline = true;
						//PDFLOGI("[mupdf.c] textcharatEx,  newline exist\n");
						return newlinechar;
					}
					ofs++;
				}
				ofs += span->len;
			}
		}
	}
	return emptychar;
}

static int
charat(fz_text_page *page, int idx)
{
	return textcharat(page, idx).c;
}

static int
charatEx(fz_text_page *page, int idx)
{
	return textcharatEx(page, idx).c;
}

static fz_bbox
bboxcharat(fz_text_page *page, int idx)
{
	return fz_round_rect(textcharat(page, idx).bbox);
}

static int
textlen(fz_text_page *page)
{
	fz_text_block *block;
	fz_text_line *line;
	fz_text_span *span;
	int len = 0;
	for (block = page->blocks; block < page->blocks + page->len; block++)
	{
		for (line = block->lines; line < block->lines + block->len; line++)
		{
			for (span = line->spans; span < line->spans + line->len; span++)
				len += span->len;
			len++; /* pseudo-newline */
		}
	}
	return len;
}


static int
match(fz_text_page *page, const char *s, int n)
{
	int orig = n;
	int c;
	while (*s) {
		s += fz_chartorune(&c, (char *)s);

		if (charat(page, n) == 10)
		{
			n++;
		}
		
		if (c == ' ' && charat(page, n) == ' ') 
		{
			while (charat(page, n) == ' ')
			{
				n++;
			}	
		} 
		else
		{
			if (tolower(c) != tolower(charat(page, n)))
			{	return 0;}
			n++;
		}
	}
	
	return n - orig;
}

/*
function name: matchEx()
*page: one page content of document
*s: want to search string by user
fz_chartorune: return value is the length of character
c: one character of search string
charatEx(page, n) : one character of page (N)
*/

static int
matchEx(fz_text_page *page, const char *s, int n)
{
	int orig = n;
	int c;
	while (*s) {
		s += fz_chartorune(&c, (char *)s);

		if (charatEx(page, n) == 10)
		{
			n++;
		}
		
		if (c == ' ' && charatEx(page, n) == ' ') 
		{
			while (charatEx(page, n) == ' ')
			{
				n++;
			}
		} 
		else 
		{
			// if user request match case, remove "tolower" function
			if (tolower(c) != tolower(charatEx(page, n)))
			{return 0;}
			n++;
		}
	}

	return n - orig;
}

static int
countOutlineItems(fz_outline *outline)
{
	int count = 0;

	while (outline)
	{
		if (outline->dest.kind == FZ_LINK_GOTO
				&& outline->dest.ld.gotor.page >= 0
				&& outline->title)
			count++;

		count += countOutlineItems(outline->down);
		outline = outline->next;
	}

	return count;
}

static int
fillInOutlineItems(JNIEnv * env, jclass olClass, jmethodID ctor, jobjectArray arr, int pos, fz_outline *outline, int level)
{
	while (outline)
	{
		if (outline->dest.kind == FZ_LINK_GOTO)
		{
			int page = outline->dest.ld.gotor.page;
			if (page >= 0 && outline->title)
			{
				jobject ol;
				jstring title = (*env)->NewStringUTF(env, outline->title);
				if (title == NULL) return -1;
				ol = (*env)->NewObject(env, olClass, ctor, level, title, page);
				if (ol == NULL) return -1;
				(*env)->SetObjectArrayElement(env, arr, pos, ol);
				(*env)->DeleteLocalRef(env, ol);
				(*env)->DeleteLocalRef(env, title);
				pos++;
			}
		}
		pos = fillInOutlineItems(env, olClass, ctor, arr, pos, outline->down, level+1);
		if (pos < 0) return -1;
		outline = outline->next;
	}

	return pos;
}

static int
fillInSearchResults(JNIEnv * env, jclass searchClass, jmethodID ctor, jobjectArray arr, int i4pos, int i4page, const char *psearchresult, float left, float top, float right, float bottom, float left2, float top2, float right2, float bottom2)
{
	jstring title = (*env)->NewStringUTF(env, psearchresult);
	if (title == NULL) 
	{
		PDFLOGI("[mupdf.c] fillInSearchResults, search title is NULL, return it \n");
		return -1;
	}
	
	jobject search = (*env)->NewObject(env, searchClass, ctor, i4page, title, left, top, right, bottom, left2, top2, right2, bottom2);
	if (search == NULL) 
	{
		PDFLOGI("[mupdf.c] fillInSearchResults, search object is NULL, return it \n");
		return -1;
	}
	
	(*env)->SetObjectArrayElement(env, arr, i4pos, search);
	
	(*env)->DeleteLocalRef(env, search);
	(*env)->DeleteLocalRef(env, title);

	PDFLOGI("[mupdf.c] fillInSearchResults, ARRAY[%d]: Page=%d, left1=%7.2f, top1=%7.2f, right1=%7.2f, bottom1=%7.2f, left2=%7.2f, top2=%7.2f, right2=%7.2f, bottom2=%7.2f, Title=%s \n", i4pos, i4page, left, top, right, bottom, left2, top2, right2, bottom2, psearchresult);
	i4pos = i4pos + 1;
	return i4pos;
}

JNIEXPORT jboolean JNICALL
Java_com_yt_reader_format_pdf_MuPDFCore_needsPasswordInternal(JNIEnv * env, jobject thiz)
{
	return fz_needs_password(doc) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_yt_reader_format_pdf_MuPDFCore_authenticatePasswordInternal(JNIEnv *env, jobject thiz, jstring password)
{
	const char *pw;
	int         result;
	pw = (*env)->GetStringUTFChars(env, password, NULL);
	PDFLOGI("[mupdf.c] Java_com_yt_reader_format_pdf_MuPDFCore_authenticatePasswordInternal start, pw is %s \n", pw);
	if (pw == NULL)
		return JNI_FALSE;

	result = fz_authenticate_password(doc, (char *)pw);
	(*env)->ReleaseStringUTFChars(env, password, pw);
	return result;
}

JNIEXPORT jboolean JNICALL
Java_com_yt_reader_format_pdf_MuPDFCore_hasOutlineInternal(JNIEnv * env, jobject thiz)
{
	fz_outline *outline = fz_load_outline(doc);
	return (outline == NULL) ? JNI_FALSE : JNI_TRUE;
}

JNIEXPORT jobjectArray JNICALL
Java_com_yt_reader_format_pdf_MuPDFCore_getOutlineInternal(JNIEnv * env, jobject thiz)
{
	PDFLOGI("[mupdf.c] Java_com_yt_reader_format_pdf_MuPDFCore_getOutlineInternal start\n");

	jclass        olClass;
	jmethodID     ctor;
	jobjectArray  arr;
	jobject       ol;
	fz_outline   *outline;
	int           nItems;

	olClass = (*env)->FindClass(env, "com/yt/reader/format/pdf/OutlineItem");
	if (olClass == NULL) return NULL;
	ctor = (*env)->GetMethodID(env, olClass, "<init>", "(ILjava/lang/String;I)V");
	if (ctor == NULL) return NULL;

	outline = fz_load_outline(doc);
	nItems = countOutlineItems(outline);

	arr = (*env)->NewObjectArray(env,
					nItems,
					olClass,
					NULL);
	if (arr == NULL) return NULL;

	return fillInOutlineItems(env, olClass, ctor, arr, 0, outline, 0) > 0
			? arr
			:NULL;
}

JNIEXPORT jobjectArray JNICALL
Java_com_yt_reader_format_pdf_MuPDFCore_search(JNIEnv * env, jobject thiz, jstring jtext)
{
	PDFLOGI("[mupdf.c] Java_com_yt_reader_format_pdf_MuPDFCore_search start\n");
	
	jclass         searchClass;
	jmethodID      ctor;
	jobjectArray   arr;
	
	fz_text_sheet *sheet = NULL;
	fz_text_page  *text = NULL;
	fz_device     *dev  = NULL;
	
	float          zoom;
	fz_matrix      ctm;

	int            pos = 0;
	int            len = 0;
	int            i = 0;
	int			   n = 0;
	int			   i4searchcount = 0;
	const char    *str;

	float left = 0;
	float top = 0;
	float right = 0;
	float bottom = 0;

	float left2 = -1;
	float top2 = -1;
	float right2 = -1;
	float bottom2 = -1;

	searchClass = (*env)->FindClass(env, "com/yt/reader/format/pdf/SearchItem");
	if (searchClass == NULL) 
	{
		LOGE("Search, searchClass is NULL, return it \n");
		return NULL;
	}
	
	ctor = (*env)->GetMethodID(env, searchClass, "<init>", "(ILjava/lang/String;FFFFFFFF)V");
	if (ctor == NULL) 	
	{
		LOGE("Search, ctor is NULL, return it \n");
		return NULL;
	}
	
	str = (*env)->GetStringUTFChars(env, jtext, NULL);
	if (str == NULL) 
	{
		LOGE("Search string is NULL, return it \n");
		return NULL;
	}
	else
	{
		LOGE("Search string is %s \n", str);
	}

	fz_var(sheet);
	fz_var(text);
	fz_var(dev);

	fz_try(ctx)
	{
		arr = (*env)->NewObjectArray(env,
							MAX_SEARCH_ARRAYS,
							searchClass,
							NULL);
		if (arr == NULL)	
		{
			LOGE("Search, new arr is NULL, return it \n");
			return NULL;
		}

		int i4arraypos = 0;
		
		unsigned char utfchar[MAX_SEARCH_STRING] = {0};
		char searchresult[MAX_SEARCH_SAVE] = {0};
		int searchresultlength = 0;

		int i4num = 0;
		int i4length = 0;

		int i4TotalPageNum = fz_count_pages(doc);
		PDFLOGI("Search string, total page number is %d \n", i4TotalPageNum);

		int i4pagenumber = 0;
		for (i4pagenumber = 0; i4pagenumber < i4TotalPageNum; i4pagenumber++)
		{
			Java_com_yt_reader_format_pdf_MuPDFCore_gotoPageInternal(env, thiz, i4pagenumber);
			fz_rect rect;	
			zoom = resolution / 72;
			ctm = fz_scale(zoom, zoom);
			rect = fz_transform_rect(ctm, currentMediabox);
			sheet = fz_new_text_sheet(ctx);
			text = fz_new_text_page(ctx, rect);
			dev  = fz_new_text_device(ctx, sheet, text);
			fz_run_page(doc, currentPage, dev, ctm, NULL);
			fz_free_device(dev);
			dev = NULL;

			len = textlen(text);
			for (pos = 0; pos < len; pos++)
			{
				n = match(text, str, pos);
				if(n > 0)
				{
					if (10 == charat(text, pos))
					{
						pos = pos + 1; // if the first character is LF, ingore it
						n = n -1;
					}
				
					fz_bbox stringBox1 = bboxcharat(text, pos);
					left = (stringBox1.x0)*g_xscale + g_wb_rect.x0;
					top = (stringBox1.y0)*g_yscale - g_wb_rect.y0;
				
					fz_bbox stringBox2 = bboxcharat(text, (pos + n - 1));
					right = (stringBox2.x1)*g_xscale + g_wb_rect.x0;
					bottom = (stringBox2.y1)*g_yscale - g_wb_rect.y0;
						
					PDFLOGI("[mupdf.c] start calc \n");
					for (i4num = pos; i4num < len; i4num++)
					{
						int i4charat = charatEx(text, i4num);

						if((true == g_fgNewline)&&((i4num - pos)<n))
						{
							fz_bbox stringBox3 = bboxcharat(text, (i4num - 1));
							right = (stringBox3.x1)*g_xscale + g_wb_rect.x0;
							bottom = (stringBox3.y1)*g_yscale - g_wb_rect.y0;
							
							fz_bbox stringBox4 = bboxcharat(text, (i4num + 1));
							left2 = (stringBox4.x0)*g_xscale + g_wb_rect.x0;
							top2 = (stringBox4.y0)*g_yscale - g_wb_rect.y0;

							fz_bbox stringBox5 = bboxcharat(text, (pos + n - 1));
							right2 = (stringBox5.x1)*g_xscale + g_wb_rect.x0;
							bottom2 = (stringBox5.y1)*g_yscale - g_wb_rect.y0;							
							
							g_fgNewline = false;
						}
						
						if(10 == i4charat)
						{
							continue; // could not store LF ascii character
						}

						int i4Result = pdf_unicode_to_utf8(i4charat, utfchar);
						PDFLOGI("[mupdf.c] i4Result = %d, charat(text, i4num) = %d, utfchar = %s, strlen(utfchar) = %d \n", i4Result, i4charat, utfchar, strlen(utfchar));			
												
						if((searchresultlength + strlen(utfchar)) < MAX_SEARCH_STRING)
						{
							strncat(searchresult, utfchar, strlen(utfchar));
							searchresultlength = searchresultlength + strlen(utfchar);
							strncpy(utfchar, "\0", MAX_SEARCH_STRING);
						}
						else
						{
							break;
						}
					}

					searchresultlength = 0;
					g_fgNewline = false;
					
					if(i4searchcount < MAX_SEARCH_ARRAYS)
					{
						i4arraypos = fillInSearchResults(env, searchClass, ctor, arr, i4arraypos, i4pagenumber, searchresult, left, top, right, bottom, left2, top2, right2, bottom2);
						if(i4arraypos == -1)
						{
							LOGE("Search, fill in string failed, return it \n");
							return NULL;
						}
						i4searchcount = i4searchcount + 1;
						strncpy(utfchar, "\0", MAX_SEARCH_STRING);
						strncpy(searchresult, "\0", MAX_SEARCH_SAVE);
					}
					else
					{
						strncpy(utfchar, "\0", MAX_SEARCH_STRING);
						strncpy(searchresult, "\0", MAX_SEARCH_SAVE);

						left = 0;
						top = 0;
						right = 0;
						bottom = 0;
						left2 = -1;
						top2 = -1;
						right2 = -1;
						bottom2 = -1;
						
						break;
					}
					pos = pos + (n - 1);
				}

				left = 0;
				top = 0;
				right = 0;
				bottom = 0;
				left2 = -1;
				top2 = -1;
				right2 = -1;
				bottom2 = -1;

				g_fgNewline = false;
	
			}	

			if(i4searchcount >= MAX_SEARCH_ARRAYS)
			{
				break;
			}
		}	
	}
	fz_always(ctx)
	{
		fz_free_text_page(ctx, text);
		fz_free_text_sheet(ctx, sheet);
		fz_free_device(dev);
	}
	fz_catch(ctx)
	{
		jclass cls;
		(*env)->ReleaseStringUTFChars(env, jtext, str);
		cls = (*env)->FindClass(env, "java/lang/OutOfMemoryError");
		if (cls != NULL)
			(*env)->ThrowNew(env, cls, "out of memory in PDFCore_search");
		(*env)->DeleteLocalRef(env, cls);
		return NULL;
	}

	(*env)->ReleaseStringUTFChars(env, jtext, str);

	i4searchcount = 0;
	
	return arr;

}

JNIEXPORT jobjectArray JNICALL
Java_com_yt_reader_format_pdf_MuPDFCore_searchPage(JNIEnv * env, jobject thiz, jstring jtext)
{
	PDFLOGI("[mupdf.c] Java_com_yt_reader_format_pdf_MuPDFCore_searchPage start\n");

	jclass         rectClass;
	jmethodID      ctor;
	jobjectArray   arr;
	jobject        rect;
	fz_text_sheet *sheet = NULL;
	fz_text_page  *text = NULL;
	fz_device     *dev  = NULL;
	float          zoom;
	fz_matrix      ctm;
	int            pos;
	int            len;
	int            i, n;
	int            hit_count = 0;
	const char    *str;

	rectClass = (*env)->FindClass(env, "android/graphics/RectF");
	if (rectClass == NULL) return NULL;
	ctor = (*env)->GetMethodID(env, rectClass, "<init>", "(FFFF)V");
	if (ctor == NULL) return NULL;
	
	str = (*env)->GetStringUTFChars(env, jtext, NULL);
	if (str == NULL) 
	{
		return NULL;
	}
	else
	{
		LOGE("Search string is %s \n", str);
	}

	fz_var(sheet);
	fz_var(text);
	fz_var(dev);

	fz_try(ctx)
	{
		fz_rect rect;

		if (hit_bbox == NULL)
			hit_bbox = fz_malloc_array(ctx, MAX_SEARCH_HITS, sizeof(*hit_bbox));

		zoom = resolution / 72;
		ctm = fz_scale(zoom, zoom);
		rect = fz_transform_rect(ctm, currentMediabox);
		sheet = fz_new_text_sheet(ctx);
		text = fz_new_text_page(ctx, rect);
		dev  = fz_new_text_device(ctx, sheet, text);
		fz_run_page(doc, currentPage, dev, ctm, NULL);
		fz_free_device(dev);
		dev = NULL;

		len = textlen(text);
		PDFLOGI("[mupdf.c] Java_com_yt_reader_format_pdf_MuPDFCore_searchPage, len = %d \n", len);
		for (pos = 0; pos < len; pos++)
		{
			fz_bbox rr = fz_empty_bbox;
			n = match(text, str, pos);
			PDFLOGI("[mupdf.c] Java_com_yt_reader_format_pdf_MuPDFCore_searchPage, n = %d \n", n);
			for (i = 0; i < n; i++)
				rr = fz_union_bbox(rr, bboxcharat(text, pos + i));

			if (!fz_is_empty_bbox(rr) && hit_count < MAX_SEARCH_HITS)
				hit_bbox[hit_count++] = rr;
		}
	}
	fz_always(ctx)
	{
		fz_free_text_page(ctx, text);
		fz_free_text_sheet(ctx, sheet);
		fz_free_device(dev);
	}
	fz_catch(ctx)
	{
		jclass cls;
		(*env)->ReleaseStringUTFChars(env, jtext, str);
		cls = (*env)->FindClass(env, "java/lang/OutOfMemoryError");
		if (cls != NULL)
			(*env)->ThrowNew(env, cls, "Out of memory in MuPDFCore_searchPage");
		(*env)->DeleteLocalRef(env, cls);

		return NULL;
	}

	(*env)->ReleaseStringUTFChars(env, jtext, str);

	arr = (*env)->NewObjectArray(env,
					hit_count,
					rectClass,
					NULL);
	if (arr == NULL) return NULL;

	for (i = 0; i < hit_count; i++) {
		rect = (*env)->NewObject(env, rectClass, ctor,
				(float) (hit_bbox[i].x0),
				(float) (hit_bbox[i].y0),
				(float) (hit_bbox[i].x1),
				(float) (hit_bbox[i].y1));
		if (rect == NULL)
			return NULL;
		(*env)->SetObjectArrayElement(env, arr, i, rect);
		(*env)->DeleteLocalRef(env, rect);
	}

	return arr;
}

JNIEXPORT void JNICALL
Java_com_yt_reader_format_pdf_MuPDFCore_destroying(JNIEnv * env, jobject thiz)
{
	PDFLOGI("[mupdf.c] Java_com_yt_reader_format_pdf_MuPDFCore_destroying start \n");
	i4totalpagenum = 0;

	fz_free(ctx, hit_bbox);
	hit_bbox = NULL;
	fz_free_display_list(ctx, currentPageList);
	currentPageList = NULL;
	if (currentPage != NULL)
	{
		fz_free_page(doc, currentPage);
		currentPage = NULL;
	}
	fz_close_document(doc);
	doc = NULL;

	LOGE("Document destroying \n");
}

JNIEXPORT jstring JNICALL
Java_com_yt_reader_format_pdf_MuPDFCore_getBookMarkStr(JNIEnv * env, jobject thiz, int pageNumber)
{
	PDFLOGI("[mupdf.c] Java_com_yt_reader_format_pdf_MuPDFCore_getBookMarkStr start \n");

	char chBookmarkStr[128] = {0};

#if SUPPORT_BOOKMARK

	strncpy(chBookmarkStr, g_chBookmarkStr, 128);
	chBookmarkStr[127] = '\0';

	// init g_chBookmarkStr to 0
	int i4i = 0;
	for(i4i = 0; i4i < 128; i4i++)
	{
		g_chBookmarkStr[i4i] = '\0';
	}
	
#endif	

	PDFLOGI("[mupdf.c] Java_com_yt_reader_format_pdf_MuPDFCore_getBookMarkStr, chBookmarkStr = %s\n", chBookmarkStr);
	return (*env)->NewStringUTF(env, chBookmarkStr);

}

JNIEXPORT jobjectArray JNICALL
Java_com_yt_reader_format_pdf_MuPDFCore_getPageLinksInternal(JNIEnv * env, jobject thiz, int pageNumber)
{
	PDFLOGI("[mupdf.c] Java_com_yt_reader_format_pdf_MuPDFCore_getPageLinksInternal start, pageNumber = %d \n", pageNumber);

	jclass       linkInfoClass;
	jmethodID    ctor;
	jobjectArray arr;
	jobject      linkInfo;
	fz_matrix    ctm;
	float        zoom;
	fz_link     *list;
	fz_link     *link;
	int          count;

	linkInfoClass = (*env)->FindClass(env, "com/yt/reader/format/pdf/LinkInfo");
	if (linkInfoClass == NULL) return NULL;
	ctor = (*env)->GetMethodID(env, linkInfoClass, "<init>", "(FFFFI)V");
	if (ctor == NULL) return NULL;

	Java_com_yt_reader_format_pdf_MuPDFCore_gotoPageInternal(env, thiz, pageNumber);
	if (currentPageNumber == -1 || currentPage == NULL)
		return NULL;

	zoom = resolution / 72;
	ctm = fz_scale(zoom, zoom);

	list = fz_load_links(doc, currentPage);
	count = 0;
	for (link = list; link; link = link->next)
	{
		if (link->dest.kind == FZ_LINK_GOTO)
			count++ ;
	}

	arr = (*env)->NewObjectArray(env, count, linkInfoClass, NULL);
	if (arr == NULL) return NULL;

	count = 0;
	for (link = list; link; link = link->next)
	{
		if (link->dest.kind == FZ_LINK_GOTO)
		{
			fz_rect rect = fz_transform_rect(ctm, link->rect);

			linkInfo = (*env)->NewObject(env, linkInfoClass, ctor,
					(float)rect.x0, (float)rect.y0, (float)rect.x1, (float)rect.y1,
					link->dest.ld.gotor.page);
			if (linkInfo == NULL) return NULL;
			(*env)->SetObjectArrayElement(env, arr, count, linkInfo);
			(*env)->DeleteLocalRef(env, linkInfo);

			count ++;
		}
	}

	return arr;
}

JNIEXPORT int JNICALL
Java_com_yt_reader_format_pdf_MuPDFCore_getPageLink(JNIEnv * env, jobject thiz, int pageNumber, float x, float y)
{
	PDFLOGI("[mupdf.c] Java_com_yt_reader_format_pdf_MuPDFCore_getPageLink start, pageNumber = %d \n", pageNumber);

	fz_matrix ctm;
	float zoom;
	fz_link *link;
	fz_point p;

	Java_com_yt_reader_format_pdf_MuPDFCore_gotoPageInternal(env, thiz, pageNumber);
	if (currentPageNumber == -1 || currentPage == NULL)
		return -1;

	p.x = x;
	p.y = y;

	/* Ultimately we should probably return a pointer to a java structure
	 * with the link details in, but for now, page number will suffice.
	 */
	zoom = resolution / 72;
	ctm = fz_scale(zoom, zoom);
	ctm = fz_invert_matrix(ctm);

	p = fz_transform_point(ctm, p);

	for (link = fz_load_links(doc, currentPage); link; link = link->next)
	{
		if (p.x >= link->rect.x0 && p.x <= link->rect.x1)
			if (p.y >= link->rect.y0 && p.y <= link->rect.y1)
				break;
	}

	if (link == NULL)
		return -1;

	if (link->dest.kind == FZ_LINK_URI)
	{
		//gotouri(link->dest.ld.uri.uri);
		return -1;
	}
	else if (link->dest.kind == FZ_LINK_GOTO)
		return link->dest.ld.gotor.page;
	return -1;
}
