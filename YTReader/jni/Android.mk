LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE                  := DeflatingDecompressor-v3
LOCAL_SRC_FILES               := DeflatingDecompressor/DeflatingDecompressor.cpp
LOCAL_LDLIBS                  := -lz -llog


include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE                  := LineBreak-v2
LOCAL_SRC_FILES               := LineBreak/LineBreaker.cpp LineBreak/liblinebreak-2.0/linebreak.c LineBreak/liblinebreak-2.0/linebreakdata.c LineBreak/liblinebreak-2.0/linebreakdef.c

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)

EXPAT_DIR                     := expat-2.0.1

LOCAL_MODULE                  := expat
LOCAL_SRC_FILES               := $(EXPAT_DIR)/lib/xmlparse.c $(EXPAT_DIR)/lib/xmlrole.c $(EXPAT_DIR)/lib/xmltok.c
LOCAL_CFLAGS                  := -DHAVE_EXPAT_CONFIG_H
LOCAL_C_INCLUDES              := $(LOCAL_PATH)/$(EXPAT_DIR)
LOCAL_EXPORT_C_INCLUDES       := $(LOCAL_PATH)/$(EXPAT_DIR)/lib

include $(BUILD_STATIC_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE                  := NativeFormats-v2
LOCAL_CFLAGS                  := -Wall
LOCAL_LDLIBS                  := -lz -llog
LOCAL_STATIC_LIBRARIES        := expat

LOCAL_SRC_FILES               := \
	NativeFormats/main.cpp \
	NativeFormats/JavaNativeFormatPlugin.cpp \
	NativeFormats/JavaPluginCollection.cpp \
	NativeFormats/util/AndroidUtil.cpp \
	NativeFormats/util/JniEnvelope.cpp \
	NativeFormats/zlibrary/core/src/constants/ZLXMLNamespace.cpp \
	NativeFormats/zlibrary/core/src/encoding/DummyEncodingConverter.cpp \
	NativeFormats/zlibrary/core/src/encoding/Utf16EncodingConverters.cpp \
	NativeFormats/zlibrary/core/src/encoding/JavaEncodingConverter.cpp \
	NativeFormats/zlibrary/core/src/encoding/ZLEncodingCollection.cpp \
	NativeFormats/zlibrary/core/src/encoding/ZLEncodingConverter.cpp \
	NativeFormats/zlibrary/core/src/filesystem/ZLDir.cpp \
	NativeFormats/zlibrary/core/src/filesystem/ZLFSManager.cpp \
	NativeFormats/zlibrary/core/src/filesystem/ZLFile.cpp \
	NativeFormats/zlibrary/core/src/filesystem/ZLInputStreamDecorator.cpp \
	NativeFormats/zlibrary/core/src/filesystem/zip/ZLGzipInputStream.cpp \
	NativeFormats/zlibrary/core/src/filesystem/zip/ZLZDecompressor.cpp \
	NativeFormats/zlibrary/core/src/filesystem/zip/ZLZipDir.cpp \
	NativeFormats/zlibrary/core/src/filesystem/zip/ZLZipEntryCache.cpp \
	NativeFormats/zlibrary/core/src/filesystem/zip/ZLZipHeader.cpp \
	NativeFormats/zlibrary/core/src/filesystem/zip/ZLZipInputStream.cpp \
	NativeFormats/zlibrary/core/src/language/ZLCharSequence.cpp \
	NativeFormats/zlibrary/core/src/language/ZLLanguageDetector.cpp \
	NativeFormats/zlibrary/core/src/language/ZLLanguageList.cpp \
	NativeFormats/zlibrary/core/src/language/ZLLanguageMatcher.cpp \
	NativeFormats/zlibrary/core/src/language/ZLStatistics.cpp \
	NativeFormats/zlibrary/core/src/language/ZLStatisticsGenerator.cpp \
	NativeFormats/zlibrary/core/src/language/ZLStatisticsItem.cpp \
	NativeFormats/zlibrary/core/src/language/ZLStatisticsXMLReader.cpp \
	NativeFormats/zlibrary/core/src/library/ZLibrary.cpp \
	NativeFormats/zlibrary/core/src/logger/ZLLogger.cpp \
	NativeFormats/zlibrary/core/src/util/ZLFileUtil.cpp \
	NativeFormats/zlibrary/core/src/util/ZLStringUtil.cpp \
	NativeFormats/zlibrary/core/src/util/ZLUnicodeUtil.cpp \
	NativeFormats/zlibrary/core/src/xml/ZLAsynchronousInputStream.cpp \
	NativeFormats/zlibrary/core/src/xml/ZLPlainAsynchronousInputStream.cpp \
	NativeFormats/zlibrary/core/src/xml/ZLXMLReader.cpp \
	NativeFormats/zlibrary/core/src/xml/expat/ZLXMLReaderInternal.cpp \
	NativeFormats/zlibrary/core/src/unix/filesystem/ZLUnixFSDir.cpp \
	NativeFormats/zlibrary/core/src/unix/filesystem/ZLUnixFSManager.cpp \
	NativeFormats/zlibrary/core/src/unix/filesystem/ZLUnixFileInputStream.cpp \
	NativeFormats/zlibrary/core/src/unix/filesystem/ZLUnixFileOutputStream.cpp \
	NativeFormats/zlibrary/core/src/unix/library/ZLUnixLibrary.cpp \
	NativeFormats/zlibrary/text/src/model/ZLCachedMemoryAllocator.cpp \
	NativeFormats/zlibrary/text/src/model/ZLTextModel.cpp \
	NativeFormats/zlibrary/text/src/model/ZLTextParagraph.cpp \
	NativeFormats/zlibrary/ui/src/android/filesystem/JavaFSDir.cpp \
	NativeFormats/zlibrary/ui/src/android/filesystem/JavaInputStream.cpp \
	NativeFormats/zlibrary/ui/src/android/filesystem/ZLAndroidFSManager.cpp \
	NativeFormats/zlibrary/ui/src/android/library/ZLAndroidLibraryImplementation.cpp \
	NativeFormats/fbreader/src/bookmodel/BookModel.cpp \
	NativeFormats/fbreader/src/bookmodel/BookReader.cpp \
	NativeFormats/fbreader/src/formats/EncodedTextReader.cpp \
	NativeFormats/fbreader/src/formats/FormatPlugin.cpp \
	NativeFormats/fbreader/src/formats/PluginCollection.cpp \
	NativeFormats/fbreader/src/formats/fb2/FB2BookReader.cpp \
	NativeFormats/fbreader/src/formats/fb2/FB2CoverReader.cpp \
	NativeFormats/fbreader/src/formats/fb2/FB2MetaInfoReader.cpp \
	NativeFormats/fbreader/src/formats/fb2/FB2Plugin.cpp \
	NativeFormats/fbreader/src/formats/fb2/FB2Reader.cpp \
	NativeFormats/fbreader/src/formats/fb2/FB2TagManager.cpp \
	NativeFormats/fbreader/src/formats/css/StyleSheetParser.cpp \
	NativeFormats/fbreader/src/formats/css/StyleSheetTable.cpp \
	NativeFormats/fbreader/src/formats/html/HtmlBookReader.cpp \
	NativeFormats/fbreader/src/formats/html/HtmlDescriptionReader.cpp \
	NativeFormats/fbreader/src/formats/html/HtmlEntityCollection.cpp \
	NativeFormats/fbreader/src/formats/html/HtmlPlugin.cpp \
	NativeFormats/fbreader/src/formats/html/HtmlReader.cpp \
	NativeFormats/fbreader/src/formats/html/HtmlReaderStream.cpp \
	NativeFormats/fbreader/src/formats/oeb/NCXReader.cpp \
	NativeFormats/fbreader/src/formats/oeb/OEBBookReader.cpp \
	NativeFormats/fbreader/src/formats/oeb/OEBCoverReader.cpp \
	NativeFormats/fbreader/src/formats/oeb/OEBMetaInfoReader.cpp \
	NativeFormats/fbreader/src/formats/oeb/OEBPlugin.cpp \
	NativeFormats/fbreader/src/formats/oeb/OEBTextStream.cpp \
	NativeFormats/fbreader/src/formats/oeb/XHTMLImageFinder.cpp \
	NativeFormats/fbreader/src/formats/rtf/RtfBookReader.cpp \
	NativeFormats/fbreader/src/formats/rtf/RtfDescriptionReader.cpp \
	NativeFormats/fbreader/src/formats/rtf/RtfPlugin.cpp \
	NativeFormats/fbreader/src/formats/rtf/RtfReader.cpp \
	NativeFormats/fbreader/src/formats/rtf/RtfReaderStream.cpp \
	NativeFormats/fbreader/src/formats/txt/PlainTextFormat.cpp \
	NativeFormats/fbreader/src/formats/txt/TxtBookReader.cpp \
	NativeFormats/fbreader/src/formats/txt/TxtPlugin.cpp \
	NativeFormats/fbreader/src/formats/txt/TxtReader.cpp \
	NativeFormats/fbreader/src/formats/util/EntityFilesCollector.cpp \
	NativeFormats/fbreader/src/formats/util/MergedStream.cpp \
	NativeFormats/fbreader/src/formats/util/MiscUtil.cpp \
	NativeFormats/fbreader/src/formats/util/XMLTextStream.cpp \
	NativeFormats/fbreader/src/formats/xhtml/XHTMLReader.cpp \
	NativeFormats/fbreader/src/library/Author.cpp \
	NativeFormats/fbreader/src/library/Book.cpp \
	NativeFormats/fbreader/src/library/Comparators.cpp \
	NativeFormats/fbreader/src/library/Library.cpp \
	NativeFormats/fbreader/src/library/Tag.cpp

LOCAL_C_INCLUDES              := \
	$(LOCAL_PATH)/NativeFormats/util \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/constants \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/encoding \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/filesystem \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/image \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/language \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/library \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/logger \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/util \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/xml \
	$(LOCAL_PATH)/NativeFormats/zlibrary/text/src/model

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE    := cr3engine-45-15

# Generate CREngine blob with statically linked libjpeg, libpng, libfreetype, chmlib
# TODO: build libraries using separate makefiles

CRFLAGS = -DLINUX=1 -DFOR_ANDROID=1 -DCR3_PATCH -DFT2_BUILD_LIBRARY=1 \
     -DDOC_DATA_COMPRESSION_LEVEL=1 -DDOC_BUFFER_SIZE=0xA00000 \
     -DENABLE_CACHE_FILE_CONTENTS_VALIDATION=1 \
     -DLDOM_USE_OWN_MEM_MAN=0 \
     -DCR3_ANTIWORD_PATCH=1 -DENABLE_ANTIWORD=1 \
     -DMAX_IMAGE_SCALE_MUL=2

CR3_ROOT = $(LOCAL_PATH)/cr3engine/

LOCAL_C_INCLUDES := \
    -I $(CR3_ROOT)/crengine/include \
    -I $(CR3_ROOT)/thirdparty/libpng \
    -I $(CR3_ROOT)/thirdparty/freetype/include \
    -I $(CR3_ROOT)/thirdparty/libjpeg \
    -I $(CR3_ROOT)/thirdparty/antiword \
    -I $(CR3_ROOT)/thirdparty/chmlib/src


LOCAL_CFLAGS += $(CRFLAGS) $(CRENGINE_INCLUDES)


CRENGINE_SRC_FILES := \
    cr3engine/crengine/src/cp_stats.cpp \
    cr3engine/crengine/src/lvstring.cpp \
    cr3engine/crengine/src/props.cpp \
    cr3engine/crengine/src/lstridmap.cpp \
    cr3engine/crengine/src/rtfimp.cpp \
    cr3engine/crengine/src/lvmemman.cpp \
    cr3engine/crengine/src/lvstyles.cpp \
    cr3engine/crengine/src/crtxtenc.cpp \
    cr3engine/crengine/src/lvtinydom.cpp \
    cr3engine/crengine/src/lvstream.cpp \
    cr3engine/crengine/src/lvxml.cpp \
    cr3engine/crengine/src/chmfmt.cpp \
    cr3engine/crengine/src/epubfmt.cpp \
    cr3engine/crengine/src/pdbfmt.cpp \
    cr3engine/crengine/src/wordfmt.cpp \
    cr3engine/crengine/src/lvstsheet.cpp \
    cr3engine/crengine/src/txtselector.cpp \
    cr3engine/crengine/src/crtest.cpp \
    cr3engine/crengine/src/lvbmpbuf.cpp \
    cr3engine/crengine/src/lvfnt.cpp \
    cr3engine/crengine/src/hyphman.cpp \
    cr3engine/crengine/src/lvfntman.cpp \
    cr3engine/crengine/src/lvimg.cpp \
    cr3engine/crengine/src/crskin.cpp \
    cr3engine/crengine/src/lvdrawbuf.cpp \
    cr3engine/crengine/src/lvdocview.cpp \
    cr3engine/crengine/src/lvpagesplitter.cpp \
    cr3engine/crengine/src/lvtextfm.cpp \
    cr3engine/crengine/src/lvrend.cpp \
    cr3engine/crengine/src/wolutil.cpp \
    cr3engine/crengine/src/hist.cpp
#    cr3engine/crengine/src/cri18n.cpp
#    cr3engine/crengine/src/crgui.cpp \

PNG_SRC_FILES := \
    cr3engine/thirdparty/libpng/pngerror.c  \
    cr3engine/thirdparty/libpng/pngget.c  \
    cr3engine/thirdparty/libpng/pngpread.c \
    cr3engine/thirdparty/libpng/pngrio.c \
    cr3engine/thirdparty/libpng/pngrutil.c \
    cr3engine/thirdparty/libpng/pngvcrd.c \
    cr3engine/thirdparty/libpng/png.c \
    cr3engine/thirdparty/libpng/pnggccrd.c \
    cr3engine/thirdparty/libpng/pngmem.c \
    cr3engine/thirdparty/libpng/pngread.c \
    cr3engine/thirdparty/libpng/pngrtran.c \
    cr3engine/thirdparty/libpng/pngset.c \
    cr3engine/thirdparty/libpng/pngtrans.c \
    cr3engine/thirdparty/libpng/pngwio.c \
    cr3engine/thirdparty/libpng/pngwtran.c

JPEG_SRC_FILES := \
    cr3engine/thirdparty/libjpeg/jcapimin.c \
    cr3engine/thirdparty/libjpeg/jchuff.c \
    cr3engine/thirdparty/libjpeg/jcomapi.c \
    cr3engine/thirdparty/libjpeg/jctrans.c \
    cr3engine/thirdparty/libjpeg/jdcoefct.c \
    cr3engine/thirdparty/libjpeg/jdmainct.c \
    cr3engine/thirdparty/libjpeg/jdpostct.c \
    cr3engine/thirdparty/libjpeg/jfdctfst.c \
    cr3engine/thirdparty/libjpeg/jidctred.c \
    cr3engine/thirdparty/libjpeg/jutils.c \
    cr3engine/thirdparty/libjpeg/jcapistd.c \
    cr3engine/thirdparty/libjpeg/jcinit.c \
    cr3engine/thirdparty/libjpeg/jcparam.c \
    cr3engine/thirdparty/libjpeg/jdapimin.c \
    cr3engine/thirdparty/libjpeg/jdcolor.c \
    cr3engine/thirdparty/libjpeg/jdmarker.c \
    cr3engine/thirdparty/libjpeg/jdsample.c \
    cr3engine/thirdparty/libjpeg/jfdctint.c \
    cr3engine/thirdparty/libjpeg/jmemmgr.c \
    cr3engine/thirdparty/libjpeg/jccoefct.c \
    cr3engine/thirdparty/libjpeg/jcmainct.c \
    cr3engine/thirdparty/libjpeg/jcphuff.c \
    cr3engine/thirdparty/libjpeg/jdapistd.c \
    cr3engine/thirdparty/libjpeg/jddctmgr.c \
    cr3engine/thirdparty/libjpeg/jdmaster.c \
    cr3engine/thirdparty/libjpeg/jdtrans.c \
    cr3engine/thirdparty/libjpeg/jidctflt.c \
    cr3engine/thirdparty/libjpeg/jmemnobs.c \
    cr3engine/thirdparty/libjpeg/jccolor.c \
    cr3engine/thirdparty/libjpeg/jcmarker.c \
    cr3engine/thirdparty/libjpeg/jcprepct.c \
    cr3engine/thirdparty/libjpeg/jdatadst.c \
    cr3engine/thirdparty/libjpeg/jdhuff.c \
    cr3engine/thirdparty/libjpeg/jdmerge.c \
    cr3engine/thirdparty/libjpeg/jerror.c \
    cr3engine/thirdparty/libjpeg/jidctfst.c \
    cr3engine/thirdparty/libjpeg/jquant1.c \
    cr3engine/thirdparty/libjpeg/jcdctmgr.c \
    cr3engine/thirdparty/libjpeg/jcmaster.c \
    cr3engine/thirdparty/libjpeg/jcsample.c \
    cr3engine/thirdparty/libjpeg/jdatasrc.c \
    cr3engine/thirdparty/libjpeg/jdinput.c \
    cr3engine/thirdparty/libjpeg/jdphuff.c \
    cr3engine/thirdparty/libjpeg/jfdctflt.c \
    cr3engine/thirdparty/libjpeg/jidctint.c \
    cr3engine/thirdparty/libjpeg/jquant2.c

FREETYPE_SRC_FILES := \
    cr3engine/thirdparty/freetype/src/autofit/autofit.c \
    cr3engine/thirdparty/freetype/src/bdf/bdf.c \
    cr3engine/thirdparty/freetype/src/cff/cff.c \
    cr3engine/thirdparty/freetype/src/base/ftbase.c \
    cr3engine/thirdparty/freetype/src/base/ftbbox.c \
    cr3engine/thirdparty/freetype/src/base/ftbdf.c \
    cr3engine/thirdparty/freetype/src/base/ftbitmap.c \
    cr3engine/thirdparty/freetype/src/base/ftgasp.c \
    cr3engine/thirdparty/freetype/src/cache/ftcache.c \
    cr3engine/thirdparty/freetype/src/base/ftglyph.c \
    cr3engine/thirdparty/freetype/src/base/ftgxval.c \
    cr3engine/thirdparty/freetype/src/gzip/ftgzip.c \
    cr3engine/thirdparty/freetype/src/base/ftinit.c \
    cr3engine/thirdparty/freetype/src/lzw/ftlzw.c \
    cr3engine/thirdparty/freetype/src/base/ftmm.c \
    cr3engine/thirdparty/freetype/src/base/ftpatent.c \
    cr3engine/thirdparty/freetype/src/base/ftotval.c \
    cr3engine/thirdparty/freetype/src/base/ftpfr.c \
    cr3engine/thirdparty/freetype/src/base/ftstroke.c \
    cr3engine/thirdparty/freetype/src/base/ftsynth.c \
    cr3engine/thirdparty/freetype/src/base/ftsystem.c \
    cr3engine/thirdparty/freetype/src/base/fttype1.c \
    cr3engine/thirdparty/freetype/src/base/ftwinfnt.c \
    cr3engine/thirdparty/freetype/src/base/ftxf86.c \
    cr3engine/thirdparty/freetype/src/winfonts/winfnt.c \
    cr3engine/thirdparty/freetype/src/pcf/pcf.c \
    cr3engine/thirdparty/freetype/src/pfr/pfr.c \
    cr3engine/thirdparty/freetype/src/psaux/psaux.c \
    cr3engine/thirdparty/freetype/src/pshinter/pshinter.c \
    cr3engine/thirdparty/freetype/src/psnames/psmodule.c \
    cr3engine/thirdparty/freetype/src/raster/raster.c \
    cr3engine/thirdparty/freetype/src/sfnt/sfnt.c \
    cr3engine/thirdparty/freetype/src/smooth/smooth.c \
    cr3engine/thirdparty/freetype/src/truetype/truetype.c \
    cr3engine/thirdparty/freetype/src/type1/type1.c \
    cr3engine/thirdparty/freetype/src/cid/type1cid.c \
    cr3engine/thirdparty/freetype/src/type42/type42.c

CHM_SRC_FILES := \
    cr3engine/thirdparty/chmlib/src/chm_lib.c \
    cr3engine/thirdparty/chmlib/src/lzx.c 

ANTIWORD_SRC_FILES := \
    cr3engine/thirdparty/antiword/asc85enc.c \
    cr3engine/thirdparty/antiword/blocklist.c \
    cr3engine/thirdparty/antiword/chartrans.c \
    cr3engine/thirdparty/antiword/datalist.c \
    cr3engine/thirdparty/antiword/depot.c \
    cr3engine/thirdparty/antiword/doclist.c \
    cr3engine/thirdparty/antiword/fail.c \
    cr3engine/thirdparty/antiword/finddata.c \
    cr3engine/thirdparty/antiword/findtext.c \
    cr3engine/thirdparty/antiword/fontlist.c \
    cr3engine/thirdparty/antiword/fonts.c \
    cr3engine/thirdparty/antiword/fonts_u.c \
    cr3engine/thirdparty/antiword/hdrftrlist.c \
    cr3engine/thirdparty/antiword/imgexam.c \
    cr3engine/thirdparty/antiword/listlist.c \
    cr3engine/thirdparty/antiword/misc.c \
    cr3engine/thirdparty/antiword/notes.c \
    cr3engine/thirdparty/antiword/options.c \
    cr3engine/thirdparty/antiword/out2window.c \
    cr3engine/thirdparty/antiword/pdf.c \
    cr3engine/thirdparty/antiword/pictlist.c \
    cr3engine/thirdparty/antiword/prop0.c \
    cr3engine/thirdparty/antiword/prop2.c \
    cr3engine/thirdparty/antiword/prop6.c \
    cr3engine/thirdparty/antiword/prop8.c \
    cr3engine/thirdparty/antiword/properties.c \
    cr3engine/thirdparty/antiword/propmod.c \
    cr3engine/thirdparty/antiword/rowlist.c \
    cr3engine/thirdparty/antiword/sectlist.c \
    cr3engine/thirdparty/antiword/stylelist.c \
    cr3engine/thirdparty/antiword/stylesheet.c \
    cr3engine/thirdparty/antiword/summary.c \
    cr3engine/thirdparty/antiword/tabstop.c \
    cr3engine/thirdparty/antiword/unix.c \
    cr3engine/thirdparty/antiword/utf8.c \
    cr3engine/thirdparty/antiword/word2text.c \
    cr3engine/thirdparty/antiword/worddos.c \
    cr3engine/thirdparty/antiword/wordlib.c \
    cr3engine/thirdparty/antiword/wordmac.c \
    cr3engine/thirdparty/antiword/wordole.c \
    cr3engine/thirdparty/antiword/wordwin.c \
    cr3engine/thirdparty/antiword/xmalloc.c

JNI_SRC_FILES := \
    cr3engine/cr3engine.cpp \
    cr3engine/cr3java.cpp \
    cr3engine/docview.cpp

LOCAL_SRC_FILES := \
    $(JNI_SRC_FILES) \
    $(CRENGINE_SRC_FILES) \
    $(FREETYPE_SRC_FILES) \
    $(PNG_SRC_FILES) \
    $(JPEG_SRC_FILES) \
    $(CHM_SRC_FILES) \
    $(ANTIWORD_SRC_FILES)

LOCAL_LDLIBS    := -lm -llog -lz -ldl -Wl
#-ljnigraphics

include $(BUILD_SHARED_LIBRARY)

#清除一些变量
include $(CLEAR_VARS)
#要生成的库名
LOCAL_MODULE    := grep
#库对应的源文件
LOCAL_SRC_FILES := \
grep/jni/alloca.c \
grep/jni/dfa.c \
grep/jni/grep.c \
grep/jni/obstack.c \
grep/jni/search.c \
grep/jni/getopt.c \
grep/jni/kwset.c \
grep/jni/regex.c \
grep/jni/grepJni.c
#libtest.so需要引用的库libdl.so:加载动态函数需要，liblog.so 日志打印需要，默认是system/lib目录下
LOCAL_LDLIBS := -ldl -llog 
#生成动态库libgrep.so
include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := libiconv

LOCAL_CFLAGS := \
  -Wno-multichar \
  -DANDROID \
  -DLIBDIR="c" \
  -DBUILDING_LIBICONV \
  -DIN_LIBRARY
  
LOCAL_SRC_FILES := \
 libiconv/jni/libcharset/lib/localcharset.c \
 libiconv/jni/lib/iconv.c \
 libiconv/jni/lib/iconvJni.c \
 libiconv/jni/lib/relocatable.c
 
LOCAL_C_INCLUDES += \
  $(LOCAL_PATH)/libiconv/jni/include \
  $(LOCAL_PATH)/libiconv/jni/libcharset \
  $(LOCAL_PATH)/libiconv/jni/lib \
  $(LOCAL_PATH)/libiconv/jni/libcharset/include \
  $(LOCAL_PATH)/libiconv/jni/srclib

LOCAL_LDLIBS := -ldl -llog 
include $(BUILD_SHARED_LIBRARY)