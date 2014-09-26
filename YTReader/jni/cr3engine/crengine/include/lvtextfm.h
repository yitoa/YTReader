/** \file lvtextfm.h
    
    \brief Text formatter API

   CoolReader Engine C-compatible text formatter API

   (c) Vadim Lopatin, 2000-2006
   This source code is distributed under the terms of
   GNU General Public License.

   See LICENSE file for details.

*/

#ifndef __LVTEXTFM_H_INCLUDED__
#define __LVTEXTFM_H_INCLUDED__

#include "lvfntman.h"
#include "lvbmpbuf.h"

// comment out following line to use old formatter
#define USE_NEW_FORMATTER 1

#ifdef __cplusplus
extern "C" {
#endif

// text flags
#define LTEXT_ALIGN_LEFT       0x0001  /**< \brief new left-aligned paragraph */
#define LTEXT_ALIGN_RIGHT      0x0002  /**< \brief new right-aligned paragraph */
#define LTEXT_ALIGN_CENTER     0x0003  /**< \brief new centered paragraph */
#define LTEXT_ALIGN_WIDTH      0x0004  /**< \brief new justified paragraph */

#define LTEXT_LAST_LINE_ALIGN_SHIFT 16

#define LTEXT_LAST_LINE_ALIGN_LEFT       0x00010000  /**< \brief last line of justified paragraph should be left-aligned */
#define LTEXT_LAST_LINE_ALIGN_RIGHT      0x00020000  /**< \brief last line of justified paragraph should be right-aligned */
#define LTEXT_LAST_LINE_ALIGN_CENTER     0x00030000  /**< \brief last line of justified paragraph should be centered */
#define LTEXT_LAST_LINE_ALIGN_WIDTH      0x00040000  /**< \brief last line of justified paragraph should be justified */


#define LTEXT_FLAG_NEWLINE     0x0007  /**< \brief new line flags mask */
#define LTEXT_FLAG_OWNTEXT     0x0008  /**< \brief store local copy of text instead of pointer */

#define LTEXT_VALIGN_MASK      0x0070  /**< \brief vertical align flags mask */
#define LTEXT_VALIGN_BASELINE  0x0000  /**< \brief baseline vertical align */
#define LTEXT_VALIGN_SUB       0x0010  /**< \brief subscript */
#define LTEXT_VALIGN_SUPER     0x0020  /**< \brief superscript */

#define LTEXT_TD_UNDERLINE     0x0100  /**< \brief underlined text */
#define LTEXT_TD_OVERLINE      0x0200  /**< \brief overlined text */
#define LTEXT_TD_LINE_THROUGH  0x0400  /**< \brief striked through text */
#define LTEXT_TD_BLINK         0x0800  /**< \brief blinking text */
#define LTEXT_TD_MASK          0x0F00  /**< \brief text decoration mask */

#define LTEXT_SRC_IS_OBJECT    0x8000  /**< \brief object (image) */
#define LTEXT_IS_LINK          0x4000  /**< \brief link */
#define LTEXT_HYPHENATE        0x1000  /**< \brief allow hyphenation */
#define LTEXT_RUNIN_FLAG       0x2000  /**< \brief element display mode is runin */

#define LTEXT_FLAG_PREFORMATTED 0x0080 /**< \brief element space mode is preformatted */


/** \brief Source text line
*/
typedef struct
{
    void *          object;			//？？？
    lInt16          margin;			//首行缩进
    lUInt8          interval;		//间隔
    lInt8           letter_spacing; //字符间距
    lUInt32         color;			//颜色
    lUInt32         bgcolor;		//背景色
    lUInt32         flags;			//标志位
    lUInt16         index;			//行号
    // move unions bottom to simplify debugging
    union {
        struct {
            lvfont_handle   font;     //字体
            const lChar16 * text;     //文本缓冲区（注意：没有结束符，打log要指定长度，转换为utf-8编码）
            lUInt16         len;      //文本字符数
            lUInt16         offset;   /**< \brief offset from node start to beginning of line */
        } t;
        struct {
            lUInt16         width;    //文本占用宽度（像素）
            lUInt16         height;   //文本占用高度
        } o;
    };
} src_text_fragment_t;//解码出来的文本，保存一行数据


/** \brief Formatted word
*/
typedef struct
{
   lUInt16  src_text_index;  //行号
   lUInt16  width;           //宽度（像素）
   lUInt16  x;               //x坐标
   lInt8    y;               //y坐标（通常为零）
   lUInt8   flags;           //标志位
   union {
          /// for text word
       struct {
           lUInt16  start;           //行号
           lUInt16  len;             //单词的字符数
       } t;
       /// for object
       struct {
           lUInt16  height;           //图片高度
       } o;
   };
   lUInt16 min_width;        //最小宽度
   lUInt16 padding;          /**< \brief 18 not used */
} formatted_word_t;//单词

/// can add space after this word
#define LTEXT_WORD_CAN_ADD_SPACE_AFTER       1
/// can break line after this word
#define LTEXT_WORD_CAN_BREAK_LINE_AFTER      2
/// can break with hyphenation after this word
#define LTEXT_WORD_CAN_HYPH_BREAK_LINE_AFTER 4
/// must break line after this word
#define LTEXT_WORD_MUST_BREAK_LINE_AFTER     8
/// object flag
#define LTEXT_WORD_IS_OBJECT         0x80
/// first word of link flag
#define LTEXT_WORD_IS_LINK_START     0x40

//#define LTEXT_BACKGROUND_MARK_FLAGS 0xFFFF0000l

/** \brief Text formatter formatted line
*/
typedef struct
{
   formatted_word_t * words;       //单词
   lInt32             word_count;  //单词数
   lUInt32            y;           //y坐标
   lUInt16            x;           //x坐标
   lUInt16            width;       //宽度
   lUInt16            height;      //高度
   lUInt16            baseline;    //y偏移
   lUInt8             flags;       //标志位
   lUInt8             align;       //对齐
} formatted_line_t;//每行分割成一个个单词

/** \brief Bookmark highlight modes.
*/
enum {
    highlight_mode_none = 0,
    highlight_mode_solid = 1,
    highlight_mode_underline = 2
};

/** \brief Text highlight options for selection, bookmarks, etc.
*/
struct text_highlight_options_t {
    lUInt32 selectionColor;    /**< selection color */
    lUInt32 commentColor;      /**< comment bookmark color */
    lUInt32 correctionColor;   /**< correction bookmark color */
    int bookmarkHighlightMode; /**< bookmark highlight mode: 0=no highlight, 1=solid fill, 2=underline */
    text_highlight_options_t() {
        selectionColor = 0x80AAAAAA;
        commentColor = 0xC0FFFF00;
        correctionColor = 0xC0FF8000;
        bookmarkHighlightMode = highlight_mode_solid;
    }
};

/** \brief Text formatter container
*/
typedef struct
{
   src_text_fragment_t * srctext;       //每行字符的文本信息，原始数据
   lInt32                srctextlen;    //总共多少行
   formatted_line_t   ** frmlines;      //格式化行
   lInt32                frmlinecount;  //格式化行数
   lUInt32               height;        //本段总高度
   lUInt16               width;         //本段总宽度
   lUInt16               page_height;   //本段最大高度
   lInt32                img_zoom_in_mode_block; /**< can zoom in block images: 0=disabled, 1=integer scale, 2=free scale */
   lInt32                img_zoom_in_scale_block; /**< max scale for block images zoom in: 1, 2, 3 */
   lInt32                img_zoom_in_mode_inline; /**< can zoom in inline images: 0=disabled, 1=integer scale, 2=free scale */
   lInt32                img_zoom_in_scale_inline; /**< max scale for inline images zoom in: 1, 2, 3 */
   lInt32                img_zoom_out_mode_block; /**< can zoom out block images: 0=disabled, 1=integer scale, 2=free scale */
   lInt32                img_zoom_out_scale_block; /**< max scale for block images zoom out: 1, 2, 3 */
   lInt32                img_zoom_out_mode_inline; /**< can zoom out inline images: 0=disabled, 1=integer scale, 2=free scale */
   lInt32                img_zoom_out_scale_inline; /**< max scale for inline images zoom out: 1, 2, 3 */
   lInt32                min_space_condensing_percent; /**< min size of space (relative to normal size) to allow fitting line by reducing of spaces */
   text_highlight_options_t highlight_options; /**< options for selection/bookmark highlighting */
} formatted_text_fragment_t;//本段数据

/**  Alloc & init formatted text buffer

    \param width is width of formatted text fragment
*/
formatted_text_fragment_t * lvtextAllocFormatter( lUInt16 width );

/** Free formatted text buffer

    dont't forget to call it when object is no longer used

    \param pbuffer is pointer to formatted text buffer
*/
void lvtextFreeFormatter( formatted_text_fragment_t * pbuffer );

/** Add source text line

    Call this function after lvtextInitFormatter for each source fragment
*/
void lvtextAddSourceLine( 
   formatted_text_fragment_t * pbuffer,
   lvfont_handle   font,     /* handle of font to draw string */
   const lChar16 * text,     /* pointer to unicode text string */
   lUInt32         len,      /* number of chars in text, 0 for auto(strlen) */
   lUInt32         color,    /* text color */
   lUInt32         bgcolor,  /* background color */
   lUInt32         flags,    /* flags */
   lUInt8          interval, /* interline space, *16 (16=single, 32=double) */
   lUInt16         margin,   /* first line margin */
   void *          object,   /* pointer to custom object */
   lUInt16         offset,    /* offset from node/object start to start of line */
   lInt8           letter_spacing
                         );

/** Add source object

    Call this function after lvtextInitFormatter for each source fragment
*/
void lvtextAddSourceObject( 
   formatted_text_fragment_t * pbuffer,
   lUInt16         width,
   lUInt16         height,
   lUInt32         flags,    /* flags */
   lUInt8          interval, /* interline space, *16 (16=single, 32=double) */
   lUInt16         margin,   /* first line margin */
   void *          object,    /* pointer to custom object */
   lInt8           letter_spacing
                         );


#ifdef __cplusplus
}

class LVDrawBuf;
class ldomMarkedRangeList;
struct img_scaling_options_t;

/* C++ wrapper class */
class LFormattedText
{
    friend class LGrayDrawBuf;
private:
    formatted_text_fragment_t * m_pbuffer;
public:
    formatted_text_fragment_t * GetBuffer() { return m_pbuffer; }

    /// set image scaling options
    void setImageScalingOptions( img_scaling_options_t * options );

    /// set space condensing line fitting option (25..100%)
    void setMinSpaceCondensingPercent(int minSpaceWidthPercent);

    /// set colors for selection and bookmarks
    void setHighlightOptions(text_highlight_options_t * options);

    void Clear()
    { 
        lUInt16 width = m_pbuffer->width;
        lvtextFreeFormatter( m_pbuffer );
        m_pbuffer = lvtextAllocFormatter( width );
    }

    void AddSourceObject(
                lUInt16         flags,    /* flags */
                lUInt8          interval, /* interline space, *16 (16=single, 32=double) */
                lUInt16         margin,   /* first line margin */
                void *          object,    /* pointer to custom object */
                lInt8           letter_spacing=0
         );

    void AddSourceLine(
           const lChar16 * text,        /* pointer to unicode text string */
           lUInt32         len,         /* number of chars in text, 0 for auto(strlen) */
           lUInt32         color,       /* text color */
           lUInt32         bgcolor,     /* background color */
           LVFont          * font,        /* font to draw string */
           lUInt32         flags=LTEXT_ALIGN_LEFT|LTEXT_FLAG_OWNTEXT,
           lUInt8          interval=16, /* interline space, *16 (16=single, 32=double) */
           lUInt16         margin=0,    /* first line margin */
           void *          object=NULL,
           lUInt32         offset=0,
           lInt8           letter_spacing=0
        )
    {
        lvtextAddSourceLine(m_pbuffer, 
            font,  //font->GetHandle()
            text, len, color, bgcolor, 
            flags, interval, margin, object, (lUInt16)offset, letter_spacing );
    }

    lUInt32 Format(lUInt16 width, lUInt16 page_height);

    int GetSrcCount()
    {
        return m_pbuffer->srctextlen;
    }

    int GetWidth()
    {
        return m_pbuffer->width;
    }

    const src_text_fragment_t * GetSrcInfo(int index)
    {
        return &m_pbuffer->srctext[index];
    }

    int GetLineCount()
    {
        return m_pbuffer->frmlinecount;
    }

    const formatted_line_t * GetLineInfo(int index)
    {
        return m_pbuffer->frmlines[index];
    }

    void Draw( LVDrawBuf * buf, int x, int y, ldomMarkedRangeList * marks,  ldomMarkedRangeList *bookmarks = NULL );

    LFormattedText() { m_pbuffer = lvtextAllocFormatter( 0 ); }

    ~LFormattedText() { lvtextFreeFormatter( m_pbuffer ); }
};

#endif

extern bool gFlgFloatingPunctuationEnabled;

#endif
