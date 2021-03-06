#include "fitz-internal.h"
#include "mupdf-internal.h"

fz_rect
pdf_to_rect(fz_context *ctx, pdf_obj *array)
{
	fz_rect r;
	float a = pdf_to_real(pdf_array_get(array, 0));
	float b = pdf_to_real(pdf_array_get(array, 1));
	float c = pdf_to_real(pdf_array_get(array, 2));
	float d = pdf_to_real(pdf_array_get(array, 3));
	r.x0 = MIN(a, c);
	r.y0 = MIN(b, d);
	r.x1 = MAX(a, c);
	r.y1 = MAX(b, d);
	return r;
}

fz_matrix
pdf_to_matrix(fz_context *ctx, pdf_obj *array)
{
	fz_matrix m;
	m.a = pdf_to_real(pdf_array_get(array, 0));
	m.b = pdf_to_real(pdf_array_get(array, 1));
	m.c = pdf_to_real(pdf_array_get(array, 2));
	m.d = pdf_to_real(pdf_array_get(array, 3));
	m.e = pdf_to_real(pdf_array_get(array, 4));
	m.f = pdf_to_real(pdf_array_get(array, 5));
	return m;
}

/* Convert Unicode/PdfDocEncoding string into utf-8 */
char *
pdf_to_utf8(fz_context *ctx, pdf_obj *src)
{
	unsigned char *srcptr = (unsigned char *) pdf_to_str_buf(src);
	char *dstptr, *dst;
	int srclen = pdf_to_str_len(src);
	int dstlen = 0;
	int ucs;
	int i;

	if (srclen >= 2 && srcptr[0] == 254 && srcptr[1] == 255)
	{
		for (i = 2; i + 1 < srclen; i += 2)
		{
			ucs = srcptr[i] << 8 | srcptr[i+1];
			dstlen += fz_runelen(ucs);
		}

		dstptr = dst = fz_malloc(ctx, dstlen + 1);

		for (i = 2; i + 1 < srclen; i += 2)
		{
			ucs = srcptr[i] << 8 | srcptr[i+1];
			dstptr += fz_runetochar(dstptr, ucs);
		}
	}
	else if (srclen >= 2 && srcptr[0] == 255 && srcptr[1] == 254)
	{
		for (i = 2; i + 1 < srclen; i += 2)
		{
			ucs = srcptr[i] | srcptr[i+1] << 8;
			dstlen += fz_runelen(ucs);
		}

		dstptr = dst = fz_malloc(ctx, dstlen + 1);

		for (i = 2; i + 1 < srclen; i += 2)
		{
			ucs = srcptr[i] | srcptr[i+1] << 8;
			dstptr += fz_runetochar(dstptr, ucs);
		}
	}
	else
	{
		for (i = 0; i < srclen; i++)
			dstlen += fz_runelen(pdf_doc_encoding[srcptr[i]]);

		dstptr = dst = fz_malloc(ctx, dstlen + 1);

		for (i = 0; i < srclen; i++)
		{
			ucs = pdf_doc_encoding[srcptr[i]];
			dstptr += fz_runetochar(dstptr, ucs);
		}
	}

	*dstptr = '\0';
	return dst;
}

/* Convert Unicode/PdfDocEncoding string into utf-8 */
char *
pdf_to_utf8_from_string(fz_context *ctx, char *src)
{
	unsigned char *srcptr = (unsigned char *) src;
	char *dstptr, *dst;
	int srclen = strlen(src);
	int dstlen = 0;
	int ucs;
	int i;

	if (srclen >= 2 && srcptr[0] == 254 && srcptr[1] == 255)
	{
		for (i = 2; i + 1 < srclen; i += 2)
		{
			ucs = srcptr[i] << 8 | srcptr[i+1];
			dstlen += fz_runelen(ucs);
		}

		dstptr = dst = fz_malloc(ctx, dstlen + 1);

		for (i = 2; i + 1 < srclen; i += 2)
		{
			ucs = srcptr[i] << 8 | srcptr[i+1];
			dstptr += fz_runetochar(dstptr, ucs);
		}
	}
	else if (srclen >= 2 && srcptr[0] == 255 && srcptr[1] == 254)
	{
		for (i = 2; i + 1 < srclen; i += 2)
		{
			ucs = srcptr[i] | srcptr[i+1] << 8;
			dstlen += fz_runelen(ucs);
		}

		dstptr = dst = fz_malloc(ctx, dstlen + 1);

		for (i = 2; i + 1 < srclen; i += 2)
		{
			ucs = srcptr[i] | srcptr[i+1] << 8;
			dstptr += fz_runetochar(dstptr, ucs);
		}
	}
	else
	{
		for (i = 0; i < srclen; i++)
			dstlen += fz_runelen(pdf_doc_encoding[srcptr[i]]);

		dstptr = dst = fz_malloc(ctx, dstlen + 1);

		for (i = 0; i < srclen; i++)
		{
			ucs = pdf_doc_encoding[srcptr[i]];
			dstptr += fz_runetochar(dstptr, ucs);
		}
	}

	*dstptr = '\0';
	return dst;
}


/* Convert Unicode/PdfDocEncoding string into ucs-2 */
unsigned short *
pdf_to_ucs2(fz_context *ctx, pdf_obj *src)
{
	unsigned char *srcptr = (unsigned char *) pdf_to_str_buf(src);
	unsigned short *dstptr, *dst;
	int srclen = pdf_to_str_len(src);
	int i;

	if (srclen >= 2 && srcptr[0] == 254 && srcptr[1] == 255)
	{
		dstptr = dst = fz_malloc_array(ctx, (srclen - 2) / 2 + 1, sizeof(short));
		for (i = 2; i + 1 < srclen; i += 2)
			*dstptr++ = srcptr[i] << 8 | srcptr[i+1];
	}
	else if (srclen >= 2 && srcptr[0] == 255 && srcptr[1] == 254)
	{
		dstptr = dst = fz_malloc_array(ctx, (srclen - 2) / 2 + 1, sizeof(short));
		for (i = 2; i + 1 < srclen; i += 2)
			*dstptr++ = srcptr[i] | srcptr[i+1] << 8;
	}
	else
	{
		dstptr = dst = fz_malloc_array(ctx, srclen + 1, sizeof(short));
		for (i = 0; i < srclen; i++)
			*dstptr++ = pdf_doc_encoding[srcptr[i]];
	}

	*dstptr = '\0';
	return dst;
}

/* Convert Unicode/PdfDocEncoding string into ucs-2 */
unsigned short *
pdf_to_ucs2_from_string(fz_context *ctx, char *src)
{
	unsigned char *srcptr = (unsigned char *) src;
	unsigned short *dstptr, *dst;
	int srclen = strlen(src);
	int i;

	if (srclen >= 2 && srcptr[0] == 254 && srcptr[1] == 255)
	{
		dstptr = dst = fz_malloc_array(ctx, (srclen - 2) / 2 + 1, sizeof(short));
		for (i = 2; i + 1 < srclen; i += 2)
			*dstptr++ = srcptr[i] << 8 | srcptr[i+1];
	}
	else if (srclen >= 2 && srcptr[0] == 255 && srcptr[1] == 254)
	{
		dstptr = dst = fz_malloc_array(ctx, (srclen - 2) / 2 + 1, sizeof(short));
		for (i = 2; i + 1 < srclen; i += 2)
			*dstptr++ = srcptr[i] | srcptr[i+1] << 8;
	}
	else
	{
		dstptr = dst = fz_malloc_array(ctx, srclen + 1, sizeof(short));
		for (i = 0; i < srclen; i++)
			*dstptr++ = pdf_doc_encoding[srcptr[i]];
	}

	*dstptr = '\0';
	return dst;
}


/* Convert UCS-2 string into PdfDocEncoding for authentication */
char *
pdf_from_ucs2(fz_context *ctx, unsigned short *src)
{
	int i, j, len;
	char *docstr;

	len = 0;
	while (src[len])
		len++;
	
	PDFLOGI("[pdf_parse.c] pdf_from_ucs2, len = %d \n", len);

	docstr = fz_malloc(ctx, len + 1);

	for (i = 0; i < len; i++)
	{
		PDFLOGI("[pdf_parse.c] pdf_from_ucs2, src[%d] = %d \n", i, src[i]);
		
		/* shortcut: check if the character has the same code point in both encodings */
		if (0 < src[i] && src[i] < 256 && pdf_doc_encoding[src[i]] == src[i]) 
		{
			docstr[i] = src[i];
			continue;
		}

		/* search through pdf_docencoding for the character's code point */
		for (j = 0; j < 256; j++)
			if (pdf_doc_encoding[j] == src[i])
				break;
		docstr[i] = j;
		
		PDFLOGI("[pdf_parse.c] pdf_from_ucs2, docstr[%d] = %d \n", i, docstr[i]);

		/* fail, if a character can't be encoded */
		if (!docstr[i])
		{
			PDFLOGI("[pdf_parse.c] pdf_from_ucs2, return NULL \n");
			fz_free(ctx, docstr);
			return NULL;
		}
	}
	docstr[len] = '\0';
	
	PDFLOGI("[pdf_parse.c] pdf_from_ucs2, docstr = %s \n", docstr);

	return docstr;
}

pdf_obj *
pdf_to_utf8_name(fz_context *ctx, pdf_obj *src)
{
	char *buf = pdf_to_utf8(ctx, src);
	pdf_obj *dst = fz_new_name(ctx, buf);
	fz_free(ctx, buf);
	return dst;
}

pdf_obj *
pdf_parse_array(pdf_document *xref, fz_stream *file, pdf_lexbuf *buf)
{
	pdf_obj *ary = NULL;
	pdf_obj *obj = NULL;
	int a = 0, b = 0, n = 0;
	int tok;
	fz_context *ctx = file->ctx;
	pdf_obj *op;

	fz_var(obj);

	ary = pdf_new_array(ctx, 4);

	fz_try(ctx)
	{
		while (1)
		{
			tok = pdf_lex(file, buf);

			if (tok != PDF_TOK_INT && tok != PDF_TOK_R)
			{
				if (n > 0)
				{
					obj = pdf_new_int(ctx, a);
					pdf_array_push(ary, obj);
					pdf_drop_obj(obj);
					obj = NULL;
				}
				if (n > 1)
				{
					obj = pdf_new_int(ctx, b);
					pdf_array_push(ary, obj);
					pdf_drop_obj(obj);
					obj = NULL;
				}
				n = 0;
			}

			if (tok == PDF_TOK_INT && n == 2)
			{
				obj = pdf_new_int(ctx, a);
				pdf_array_push(ary, obj);
				pdf_drop_obj(obj);
				obj = NULL;
				a = b;
				n --;
			}

			switch (tok)
			{
			case PDF_TOK_CLOSE_ARRAY:
				op = ary;
				goto end;

			case PDF_TOK_INT:
				if (n == 0)
					a = buf->i;
				if (n == 1)
					b = buf->i;
				n ++;
				break;

			case PDF_TOK_R:
				if (n != 2)
					fz_throw(ctx, "cannot parse indirect reference in array");
				obj = pdf_new_indirect(ctx, a, b, xref);
				pdf_array_push(ary, obj);
				pdf_drop_obj(obj);
				obj = NULL;
				n = 0;
				break;

			case PDF_TOK_OPEN_ARRAY:
				obj = pdf_parse_array(xref, file, buf);
				pdf_array_push(ary, obj);
				pdf_drop_obj(obj);
				obj = NULL;
				break;

			case PDF_TOK_OPEN_DICT:
				obj = pdf_parse_dict(xref, file, buf);
				pdf_array_push(ary, obj);
				pdf_drop_obj(obj);
				obj = NULL;
				break;

			case PDF_TOK_NAME:
				obj = fz_new_name(ctx, buf->scratch);
				pdf_array_push(ary, obj);
				pdf_drop_obj(obj);
				obj = NULL;
				break;
			case PDF_TOK_REAL:
				obj = pdf_new_real(ctx, buf->f);
				pdf_array_push(ary, obj);
				pdf_drop_obj(obj);
				obj = NULL;
				break;
			case PDF_TOK_STRING:
				obj = pdf_new_string(ctx, buf->scratch, buf->len);
				pdf_array_push(ary, obj);
				pdf_drop_obj(obj);
				obj = NULL;
				break;
			case PDF_TOK_TRUE:
				obj = pdf_new_bool(ctx, 1);
				pdf_array_push(ary, obj);
				pdf_drop_obj(obj);
				obj = NULL;
				break;
			case PDF_TOK_FALSE:
				obj = pdf_new_bool(ctx, 0);
				pdf_array_push(ary, obj);
				pdf_drop_obj(obj);
				obj = NULL;
				break;
			case PDF_TOK_NULL:
				obj = pdf_new_null(ctx);
				pdf_array_push(ary, obj);
				pdf_drop_obj(obj);
				obj = NULL;
				break;

			default:
				fz_throw(ctx, "cannot parse token in array");
			}
		}
end:
		{}
	}
	fz_catch(ctx)
	{
		pdf_drop_obj(obj);
		pdf_drop_obj(ary);
		fz_throw(ctx, "cannot parse array");
	}
	return op;
}

pdf_obj *
pdf_parse_dict(pdf_document *xref, fz_stream *file, pdf_lexbuf *buf)
{
	pdf_obj *dict;
	pdf_obj *key = NULL;
	pdf_obj *val = NULL;
	int tok;
	int a, b;
	fz_context *ctx = file->ctx;

	dict = pdf_new_dict(ctx, 8);

	fz_var(key);
	fz_var(val);

	fz_try(ctx)
	{
		while (1)
		{
			tok = pdf_lex(file, buf);
	skip:
			if (tok == PDF_TOK_CLOSE_DICT)
				break;

			/* for BI .. ID .. EI in content streams */
			if (tok == PDF_TOK_KEYWORD && !strcmp(buf->scratch, "ID"))
				break;

			if (tok != PDF_TOK_NAME)
				fz_throw(ctx, "invalid key in dict");

			key = fz_new_name(ctx, buf->scratch);

			tok = pdf_lex(file, buf);

			switch (tok)
			{
			case PDF_TOK_OPEN_ARRAY:
				val = pdf_parse_array(xref, file, buf);
				break;

			case PDF_TOK_OPEN_DICT:
				val = pdf_parse_dict(xref, file, buf);
				break;

			case PDF_TOK_NAME: val = fz_new_name(ctx, buf->scratch); break;
			case PDF_TOK_REAL: val = pdf_new_real(ctx, buf->f); break;
			case PDF_TOK_STRING: val = pdf_new_string(ctx, buf->scratch, buf->len); break;
			case PDF_TOK_TRUE: val = pdf_new_bool(ctx, 1); break;
			case PDF_TOK_FALSE: val = pdf_new_bool(ctx, 0); break;
			case PDF_TOK_NULL: val = pdf_new_null(ctx); break;

			case PDF_TOK_INT:
				/* 64-bit to allow for numbers > INT_MAX and overflow */
				a = buf->i;
				tok = pdf_lex(file, buf);
				if (tok == PDF_TOK_CLOSE_DICT || tok == PDF_TOK_NAME ||
					(tok == PDF_TOK_KEYWORD && !strcmp(buf->scratch, "ID")))
				{
					val = pdf_new_int(ctx, a);
					fz_dict_put(dict, key, val);
					pdf_drop_obj(val);
					val = NULL;
					pdf_drop_obj(key);
					key = NULL;
					goto skip;
				}
				if (tok == PDF_TOK_INT)
				{
					b = buf->i;
					tok = pdf_lex(file, buf);
					if (tok == PDF_TOK_R)
					{
						val = pdf_new_indirect(ctx, a, b, xref);
						break;
					}
				}
				fz_throw(ctx, "invalid indirect reference in dict");

			default:
				fz_throw(ctx, "unknown token in dict");
			}

			fz_dict_put(dict, key, val);
			pdf_drop_obj(val);
			val = NULL;
			pdf_drop_obj(key);
			key = NULL;
		}
	}
	fz_catch(ctx)
	{
		pdf_drop_obj(dict);
		pdf_drop_obj(key);
		pdf_drop_obj(val);
		fz_throw(ctx, "cannot parse dict");
	}
	return dict;
}

pdf_obj *
pdf_parse_stm_obj(pdf_document *xref, fz_stream *file, pdf_lexbuf *buf)
{
	int tok;
	fz_context *ctx = file->ctx;

	tok = pdf_lex(file, buf);
	/* RJW: "cannot parse token in object stream") */

	switch (tok)
	{
	case PDF_TOK_OPEN_ARRAY:
		return pdf_parse_array(xref, file, buf);
		/* RJW: "cannot parse object stream" */
	case PDF_TOK_OPEN_DICT:
		return pdf_parse_dict(xref, file, buf);
		/* RJW: "cannot parse object stream" */
	case PDF_TOK_NAME: return fz_new_name(ctx, buf->scratch); break;
	case PDF_TOK_REAL: return pdf_new_real(ctx, buf->f); break;
	case PDF_TOK_STRING: return pdf_new_string(ctx, buf->scratch, buf->len); break;
	case PDF_TOK_TRUE: return pdf_new_bool(ctx, 1); break;
	case PDF_TOK_FALSE: return pdf_new_bool(ctx, 0); break;
	case PDF_TOK_NULL: return pdf_new_null(ctx); break;
	case PDF_TOK_INT: return pdf_new_int(ctx, buf->i); break;
	default: fz_throw(ctx, "unknown token in object stream");
	}
	return NULL; /* Stupid MSVC */
}

pdf_obj *
pdf_parse_ind_obj(pdf_document *xref,
	fz_stream *file, pdf_lexbuf *buf,
	int *onum, int *ogen, int *ostmofs)
{
	pdf_obj *obj = NULL;
	int num = 0, gen = 0, stm_ofs;
	int tok;
	int a, b;
	fz_context *ctx = file->ctx;

	fz_var(obj);

	tok = pdf_lex(file, buf);
	/* RJW: cannot parse indirect object (%d %d R)", num, gen */
	if (tok != PDF_TOK_INT)
		fz_throw(ctx, "expected object number (%d %d R)", num, gen);
	num = buf->i;

	tok = pdf_lex(file, buf);
	/* RJW: "cannot parse indirect object (%d %d R)", num, gen */
	if (tok != PDF_TOK_INT)
		fz_throw(ctx, "expected generation number (%d %d R)", num, gen);
	gen = buf->i;

	tok = pdf_lex(file, buf);
	/* RJW: "cannot parse indirect object (%d %d R)", num, gen */
	if (tok != PDF_TOK_OBJ)
		fz_throw(ctx, "expected 'obj' keyword (%d %d R)", num, gen);

	tok = pdf_lex(file, buf);
	/* RJW: "cannot parse indirect object (%d %d R)", num, gen */

	switch (tok)
	{
	case PDF_TOK_OPEN_ARRAY:
		obj = pdf_parse_array(xref, file, buf);
		/* RJW: "cannot parse indirect object (%d %d R)", num, gen */
		break;

	case PDF_TOK_OPEN_DICT:
		obj = pdf_parse_dict(xref, file, buf);
		/* RJW: "cannot parse indirect object (%d %d R)", num, gen */
		break;

	case PDF_TOK_NAME: obj = fz_new_name(ctx, buf->scratch); break;
	case PDF_TOK_REAL: obj = pdf_new_real(ctx, buf->f); break;
	case PDF_TOK_STRING: obj = pdf_new_string(ctx, buf->scratch, buf->len); break;
	case PDF_TOK_TRUE: obj = pdf_new_bool(ctx, 1); break;
	case PDF_TOK_FALSE: obj = pdf_new_bool(ctx, 0); break;
	case PDF_TOK_NULL: obj = pdf_new_null(ctx); break;

	case PDF_TOK_INT:
		a = buf->i;
		tok = pdf_lex(file, buf);
		/* "cannot parse indirect object (%d %d R)", num, gen */
		if (tok == PDF_TOK_STREAM || tok == PDF_TOK_ENDOBJ)
		{
			obj = pdf_new_int(ctx, a);
			goto skip;
		}
		if (tok == PDF_TOK_INT)
		{
			b = buf->i;
			tok = pdf_lex(file, buf);
			/* RJW: "cannot parse indirect object (%d %d R)", num, gen); */
			if (tok == PDF_TOK_R)
			{
				obj = pdf_new_indirect(ctx, a, b, xref);
				break;
			}
		}
		fz_throw(ctx, "expected 'R' keyword (%d %d R)", num, gen);

	case PDF_TOK_ENDOBJ:
		obj = pdf_new_null(ctx);
		goto skip;

	default:
		fz_throw(ctx, "syntax error in object (%d %d R)", num, gen);
	}

	fz_try(ctx)
	{
		tok = pdf_lex(file, buf);
	}
	fz_catch(ctx)
	{
		pdf_drop_obj(obj);
		fz_throw(ctx, "cannot parse indirect object (%d %d R)", num, gen);
	}

skip:
	if (tok == PDF_TOK_STREAM)
	{
		int c = fz_read_byte(file);
		while (c == ' ')
			c = fz_read_byte(file);
		if (c == '\r')
		{
			c = fz_peek_byte(file);
			if (c != '\n')
				fz_warn(ctx, "line feed missing after stream begin marker (%d %d R)", num, gen);
			else
				fz_read_byte(file);
		}
		stm_ofs = fz_tell(file);
	}
	else if (tok == PDF_TOK_ENDOBJ)
	{
		stm_ofs = 0;
	}
	else
	{
		fz_warn(ctx, "expected 'endobj' or 'stream' keyword (%d %d R)", num, gen);
		stm_ofs = 0;
	}

	if (onum) *onum = num;
	if (ogen) *ogen = gen;
	if (ostmofs) *ostmofs = stm_ofs;
	return obj;
}

int
pdf_unicode_to_utf8(unsigned long unic, unsigned char *pcoutput)
{
	//PDFLOGI("[pdf_parse.c] pdf_unicode_to_utf8, start, unic = 0x%x \n", unic);	

	if(NULL == pcoutput)
	{
		PDFLOGI("[pdf_parse.c] pdf_unicode_to_utf8, pcoutput is NULL, return error \n");	
		return 0;
	}

    if ( unic <= 0x0000007F )
    {
        // * U-00000000 - U-0000007F:  0xxxxxxx
        *pcoutput = (unic & 0x7F);
		return 1;
    }
    else if ( unic >= 0x00000080 && unic <= 0x000007FF )
    {
        // * U-00000080 - U-000007FF:  110xxxxx 10xxxxxx
        *(pcoutput+1) = (unic & 0x3F) | 0x80;
        *pcoutput = ((unic >> 6) & 0x1F) | 0xC0;
		return 2;
    }
    else if ( unic >= 0x00000800 && unic <= 0x0000FFFF )
    {
        // * U-00000800 - U-0000FFFF:  1110xxxx 10xxxxxx 10xxxxxx
        *(pcoutput+2) = (unic & 0x3F) | 0x80;
        *(pcoutput+1) = ((unic >>  6) & 0x3F) | 0x80;
        *pcoutput= ((unic >> 12) & 0x0F) | 0xE0;
		return 3;
    }
    else if ( unic >= 0x00010000 && unic <= 0x001FFFFF )
    {
        // * U-00010000 - U-001FFFFF:  11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
        *(pcoutput+3) = (unic & 0x3F) | 0x80;
        *(pcoutput+2) = ((unic >>  6) & 0x3F) | 0x80;
        *(pcoutput+1) = ((unic >> 12) & 0x3F) | 0x80;
        *pcoutput = ((unic >> 18) & 0x07) | 0xF0;
		return 4;
    }
    else if ( unic >= 0x00200000 && unic <= 0x03FFFFFF )
    {
        // * U-00200000 - U-03FFFFFF:  111110xx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
        *(pcoutput+4) = (unic & 0x3F) | 0x80;
        *(pcoutput+3) = ((unic >>  6) & 0x3F) | 0x80;
        *(pcoutput+2) = ((unic >> 12) & 0x3F) | 0x80;
        *(pcoutput+1) = ((unic >> 18) & 0x3F) | 0x80;
        *pcoutput = ((unic >> 24) & 0x03) | 0xF8;
		return 5;
    }
    else if ( unic >= 0x04000000 && unic <= 0x7FFFFFFF )
    {
        // * U-04000000 - U-7FFFFFFF:  1111110x 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
        *(pcoutput+5) = (unic & 0x3F) | 0x80;
        *(pcoutput+4) = ((unic >>  6) & 0x3F) | 0x80;
        *(pcoutput+3) = ((unic >> 12) & 0x3F) | 0x80;
        *(pcoutput+2) = ((unic >> 18) & 0x3F) | 0x80;
        *(pcoutput+1) = ((unic >> 24) & 0x3F) | 0x80;
        *pcoutput = ((unic >> 30) & 0x01) | 0xFC;
		return 6;
    }
	return 0;
}



