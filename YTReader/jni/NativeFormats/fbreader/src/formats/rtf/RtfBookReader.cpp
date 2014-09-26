/*
 * Copyright (C) 2004-2012 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

#include <cctype>

#include <ZLStringUtil.h>
#include <ZLFileImage.h>
#include <ZLTextStyleEntry.h>

#include "RtfBookReader.h"
#include "../../bookmodel/BookModel.h"
#include <android/log.h>
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "ytreader", __VA_ARGS__)

RtfBookReader::RtfBookReader(BookModel &model, const std::string &encoding) : RtfReader(encoding), myBookReader(model) {
}

static const size_t maxBufferSize = 2048;//可能导致文字丢失

void RtfBookReader::addCharData(const char *data, size_t len, bool convert) {
	if (myCurrentState.ReadText) {
		if (convert || myConverter.isNull()) {
			myOutputBuffer.append(data, len);
			if (myOutputBuffer.size() >= maxBufferSize) {
				flushBuffer();
			}
		} else {
			flushBuffer();
			std::string newString(data, len);
			characterDataHandler(newString);
		}
	}
}

void RtfBookReader::flushBuffer() {
	if (!myOutputBuffer.empty()) {
		if (myCurrentState.ReadText) {		
			if (!myConverter.isNull()) {
				static std::string newString;
				//gb2312编码
				/*char aa1[1024];
				int len1 = strlen(myOutputBuffer.data());
				for (int i = 0; i < len1; i++) {
					char a[5];
					sprintf(a, "%x", myOutputBuffer.data()[i]);
					if (i % 2 == 0)
						strcat(aa1, "\n");
					strcat(aa1, a);
				}
				strcat(aa1, "\n");
				strcat(aa1, myOutputBuffer.data());
				LOGD(aa1);*/
				myConverter->convert(newString, myOutputBuffer.data(), myOutputBuffer.data() + myOutputBuffer.length());
				//utf-8编码
				/*char aa[1024];
				int len = strlen(newString.data());
				for (int i = 0; i < len; i++) {
					char a[5];
					sprintf(a, "%x", newString.data()[i]);
					if (i % 3 == 0)
						strcat(aa, "\n");
					strcat(aa, a);
				}
				strcat(aa, "\n");
				strcat(aa, newString.data());
				LOGD(aa);*/
				characterDataHandler(newString);
				newString.erase();
			} else {
				characterDataHandler(myOutputBuffer);
			}
		}
		myOutputBuffer.erase();
	}
}

void RtfBookReader::switchDestination(DestinationType destination, bool on) {
	switch (destination) {
		case DESTINATION_NONE:
			break;
		case DESTINATION_SKIP:
		case DESTINATION_INFO:
		case DESTINATION_TITLE:
		case DESTINATION_AUTHOR:
		case DESTINATION_STYLESHEET:
			myCurrentState.ReadText = !on;
			break;
		case DESTINATION_PICTURE:
			if (on) {
				flushBuffer();
				if (myBookReader.paragraphIsOpen()) {
					myBookReader.endParagraph();
				}
			}
			myCurrentState.ReadText = !on;
			break;
		case DESTINATION_FOOTNOTE:
			flushBuffer();
			if (on) {
				std::string id;
				ZLStringUtil::appendNumber(id, myFootnoteIndex++);
			
				myStateStack.push(myCurrentState);
				myCurrentState.Id = id;
				myCurrentState.ReadText = true;
				
				myBookReader.addHyperlinkControl(FOOTNOTE, id);				
				myBookReader.addData(id);
				myBookReader.addControl(FOOTNOTE, false);
				
				myBookReader.setFootnoteTextModel(id);
				myBookReader.pushKind(REGULAR);
				myBookReader.beginParagraph();
			} else {
				myBookReader.endParagraph();
				myBookReader.popKind();
				
				if (!myStateStack.empty()) {
					myCurrentState = myStateStack.top();
					myStateStack.pop();
				}
				
				if (myStateStack.empty()) {
					myBookReader.setMainTextModel();
				} else {
					myBookReader.setFootnoteTextModel(myCurrentState.Id);
				}
			}
			break;
	}
}

void RtfBookReader::insertImage(const std::string &mimeType, const std::string &fileName, size_t startOffset, size_t size) {
	std::string id;
	ZLStringUtil::appendNumber(id, myImageIndex++);
	myBookReader.addImageReference(id, 0, false);	 
	const ZLFile file(fileName, mimeType);
	myBookReader.addImage(id, new ZLFileImage(file, "hex", startOffset, size));
}

/**
 * 文本内容，utf-8编码
 */
bool RtfBookReader::characterDataHandler(std::string &str) {
	if (myCurrentState.ReadText) {
		if (!myBookReader.paragraphIsOpen()) {
			myBookReader.beginParagraph();
		}
		/*char aa[1024];
		int len = strlen(str.data());
		for (int i = 0; i < len; i++) {
			char a[5];
			sprintf(a, "%x", str.data()[i]);
			if(i%3 == 0)strcat(aa, "\n");
			strcat(aa, a);
		}
		strcat(aa, "\n");
		strcat(aa, str.data());
		LOGD(aa);*/
		std::string outData;
		parseUnicode2UTF8(str.data(), outData);
		myBookReader.addData(outData);
		outData.erase();
	}
	return true;
}

void RtfBookReader::parseUnicode2UTF8(const char *inChar, std::string &outData) {
	int len = strlen(inChar);
	enum {
		READ_NORMAL_DATA,
		READ_UN_TEXT
	} parserState = READ_NORMAL_DATA;
	std::string hexString;
	const char *ptr = inChar;
	const char *end = inChar + len -1;
	//LOGD(inChar);
	while (ptr <= end) {
		switch (parserState) {
		case READ_UN_TEXT: {
			hexString += *ptr;
			if (' ' == *(ptr + 1)) {
				int num = atoi(hexString.data());
				/*char xx[15];
				 sprintf(xx, "\\u%d\n", num);
				 LOGD(xx);*/
				parserState = READ_NORMAL_DATA;
				hexString.erase();
				char *pOutput = (char *) malloc(4);
				memset(pOutput, 0, 4);
				enc_unicode_to_utf8_one(num, pOutput);
				outData.append(pOutput);
				free(pOutput);
				++ptr;
			}
			++ptr;
			break;
		}
		case READ_NORMAL_DATA: {
			if ('\\' == *ptr && 'u' == *(ptr + 1)) {
				parserState = READ_UN_TEXT;
				ptr = ptr + 2;
				break;
			}
			outData += *ptr;
			++ptr;
			break;
		}
		}
	}
	//LOGD(outData.data());
}

bool RtfBookReader::readDocument(const ZLFile &file) {
	myImageIndex = 0;
	myFootnoteIndex = 1;

	myCurrentState.ReadText = true;

	myBookReader.setMainTextModel();
	myBookReader.pushKind(REGULAR);
	myBookReader.beginParagraph();

	bool code = RtfReader::readDocument(file);

	flushBuffer();
	myBookReader.endParagraph();
	while (!myStateStack.empty()) {
		myStateStack.pop();
	}

	return code;
}

void RtfBookReader::setFontProperty(FontProperty property) {
	if (!myCurrentState.ReadText) {
		//DPRINT("change style not in text.\n");
		return;
	}
	flushBuffer();
					
	switch (property) {
		case FONT_BOLD:
			if (myState.Bold) {
				myBookReader.pushKind(STRONG);
			} else {
				myBookReader.popKind();
			}
			myBookReader.addControl(STRONG, myState.Bold);
			break;
		case FONT_ITALIC:
			if (myState.Italic) {
				if (!myState.Bold) {				
					//DPRINT("add style emphasis.\n");
					myBookReader.pushKind(EMPHASIS);
					myBookReader.addControl(EMPHASIS, true);
				} else {
					//DPRINT("add style emphasis and strong.\n");
					myBookReader.popKind();
					myBookReader.addControl(STRONG, false);
					
					myBookReader.pushKind(EMPHASIS);
					myBookReader.addControl(EMPHASIS, true);
					myBookReader.pushKind(STRONG);
					myBookReader.addControl(STRONG, true);
				}
			} else {
				if (!myState.Bold) {				
					//DPRINT("remove style emphasis.\n");
					myBookReader.addControl(EMPHASIS, false);
					myBookReader.popKind();
				} else {
					//DPRINT("remove style strong n emphasis, add strong.\n");
					myBookReader.addControl(STRONG, false);
					myBookReader.popKind();
					myBookReader.addControl(EMPHASIS, false);
					myBookReader.popKind();
					
					myBookReader.pushKind(STRONG);
					myBookReader.addControl(STRONG, true);
				}
			}
			break;
		case FONT_UNDERLINED:
			break;
	}
}

void RtfBookReader::newParagraph() {
	flushBuffer();
	myBookReader.endParagraph();
	myBookReader.beginParagraph();
	if (myState.Alignment != ALIGN_UNDEFINED) {
		setAlignment();
	}
}

void RtfBookReader::setEncoding(int) {
}

void RtfBookReader::setAlignment() {
	ZLTextStyleEntry entry;
	entry.setAlignmentType(myState.Alignment);
	myBookReader.addStyleEntry(entry);
	// TODO: call addStyleCloseEntry somewhere (?)
}

// #c---
/*****************************************************************************
 * 将一个字符的Unicode(UCS-2和UCS-4)编码转换成UTF-8编码.
 *
 * 参数:
 *    unic     字符的Unicode编码值
 *    pOutput  指向输出的用于存储UTF8编码值的缓冲区的指针
 *    outsize  pOutput缓冲的大小
 *
 * 返回值:
 *    返回转换后的字符的UTF8编码所占的字节数, 如果出错则返回 0 .
 *
 * 注意:
 *     1. UTF8没有字节序问题, 但是Unicode有字节序要求;
 *        字节序分为大端(Big Endian)和小端(Little Endian)两种;
 *        在Intel处理器中采用小端法表示, 在此采用小端法表示. (低地址存低位)
 *     2. 请保证 pOutput 缓冲区有最少有 6 字节的空间大小!
 ****************************************************************************/
int RtfBookReader::enc_unicode_to_utf8_one(long unic, char *pOutput)
{
    if ( unic <= 0x0000007F )
    {
        // * U-00000000 - U-0000007F:  0xxxxxxx
        *pOutput     = (unic & 0x7F);
        return 1;
    }
    else if ( unic >= 0x00000080 && unic <= 0x000007FF )
    {
        // * U-00000080 - U-000007FF:  110xxxxx 10xxxxxx
        *(pOutput+1) = (unic & 0x3F) | 0x80;
        *pOutput     = ((unic >> 6) & 0x1F) | 0xC0;
        return 2;
    }
    else if ( unic >= 0x00000800 && unic <= 0x0000FFFF )
    {
        // * U-00000800 - U-0000FFFF:  1110xxxx 10xxxxxx 10xxxxxx
        *(pOutput+2) = (unic & 0x3F) | 0x80;
        *(pOutput+1) = ((unic >>  6) & 0x3F) | 0x80;
        *pOutput     = ((unic >> 12) & 0x0F) | 0xE0;
        return 3;
    }
    else if ( unic >= 0x00010000 && unic <= 0x001FFFFF )
    {
        // * U-00010000 - U-001FFFFF:  11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
        *(pOutput+3) = (unic & 0x3F) | 0x80;
        *(pOutput+2) = ((unic >>  6) & 0x3F) | 0x80;
        *(pOutput+1) = ((unic >> 12) & 0x3F) | 0x80;
        *pOutput     = ((unic >> 18) & 0x07) | 0xF0;
        return 4;
    }
    else if ( unic >= 0x00200000 && unic <= 0x03FFFFFF )
    {
        // * U-00200000 - U-03FFFFFF:  111110xx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
        *(pOutput+4) = (unic & 0x3F) | 0x80;
        *(pOutput+3) = ((unic >>  6) & 0x3F) | 0x80;
        *(pOutput+2) = ((unic >> 12) & 0x3F) | 0x80;
        *(pOutput+1) = ((unic >> 18) & 0x3F) | 0x80;
        *pOutput     = ((unic >> 24) & 0x03) | 0xF8;
        return 5;
    }
    else if ( unic >= 0x04000000 && unic <= 0x7FFFFFFF )
    {
        // * U-04000000 - U-7FFFFFFF:  1111110x 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
        *(pOutput+5) = (unic & 0x3F) | 0x80;
        *(pOutput+4) = ((unic >>  6) & 0x3F) | 0x80;
        *(pOutput+3) = ((unic >> 12) & 0x3F) | 0x80;
        *(pOutput+2) = ((unic >> 18) & 0x3F) | 0x80;
        *(pOutput+1) = ((unic >> 24) & 0x3F) | 0x80;
        *pOutput     = ((unic >> 30) & 0x01) | 0xFC;
        return 6;
    }

    return 0;
}
// #c---end
