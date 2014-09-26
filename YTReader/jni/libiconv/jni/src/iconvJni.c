#include <string.h>
#include <jni.h>
#include <dlfcn.h>
#include <android/log.h>
#include <stdlib.h>
#include <stdio.h>
#include <locale.h>
#include <iconv.h>
#define MAXLEN 256

//#define  LOG_TAG    "iconvJni"
//#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
//#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

int covert(const char *dstCode, const char *srcCode, char *input, size_t ilen, char *output, size_t olen)
{
    char **pin  = &input;
    char **pout = &output;

    //打开编码流
    iconv_t cd = iconv_open(dstCode, srcCode);
    if ((iconv_t)-1 == cd)
    {
        printf("===> iconv_open is Failed!\n");

        return -1;
    }

    const int argument = 1;

    //设置iconv行为
    int iRet = iconvctl(cd, ICONV_SET_DISCARD_ILSEQ, (void*)&argument);    //忽略无效序列并继续转换
    if (0 != iRet)
    {
        printf("===> iconvctl is Failed!\n");

        return -1;
    }
    //printf("===> argument = %d\n", argument);

    //编码格式转换
    if (iconv(cd, (char**)pin, &ilen, pout, &olen))
    {
        printf("===> iconv is Failed!\n");

        return -1;
    }

    //关闭编码流
    iconv_close(cd);

    return 0;
}

//读文件
int ReadFile(FILE *fp, size_t readLen, char *outBuf)
{
    size_t readNum = 0;
    readNum = fread(outBuf, 1, readLen, fp);
    if (0 >= readNum)
    {
        printf("===> fread is Failed!\n");

        return -1;
    }

    return 0;
}

//写文件
int WriteFile(const char *fileName, const char *openMode, size_t writeLen, char *inBuf)
{
    FILE *fpWrite = fopen(fileName, openMode);
    if (NULL == fpWrite)
    {
        printf("===> open file: %s is Failed!\n", fileName);

        return -1;
    }

    size_t writeNum = 0;
    writeNum = fwrite(inBuf, 1, writeLen, fpWrite);
    if (0 >= writeLen)
    {
        printf("===> Write file: %s is Failed!\n", fileName);

        fclose(fpWrite);
        return -1;
    }

    fclose(fpWrite);
    return 0;
}

myconvert(char* fromCode, char* toCode, char* fpath, char* tpath)
{
    char convertAfterBuf[MAXLEN];
    char convertBeforeBuf[MAXLEN];
    memset(convertBeforeBuf, 0, MAXLEN);
    memset(convertAfterBuf, 0, MAXLEN);

    //打开要读的文件
    FILE *fpRead = fopen(fpath, "a+");
    if (NULL == fpRead)
    {
        printf("===> open file: %s is Failed!\n", fpath);

        return -1;
    }

    while(1)
    {
        memset(convertBeforeBuf, 0, MAXLEN);
        memset(convertAfterBuf, 0, MAXLEN);

        //设置读文件句柄偏移量，紧接上次读的位置
        int iRet = fseek(fpRead, 0, SEEK_CUR);
        if (0 != iRet)
        {
            printf("===> fseek is Failed!\n");

            return -1;
        }

        //读文件
        iRet = ReadFile(fpRead, (MAXLEN-1), convertBeforeBuf);
        if (0 != iRet)
        {
            printf("===> ReadFile is Failed!\n");

            return -1;
        }
        printf("===> len: %d\n", strlen(convertBeforeBuf));
        printf("===> convertBeforeBuf: \n%s\n\n", convertBeforeBuf);

        //编码格式转换
        iRet = covert(toCode, fromCode, convertBeforeBuf, strlen(convertBeforeBuf), convertAfterBuf, MAXLEN);
        if (0 != iRet)
        {
            printf("===> convert is Failed!\n");

            return -1;
        }

        //写文件
        iRet = WriteFile(tpath, "a+", strlen(convertAfterBuf), convertAfterBuf);
        if (0 != iRet)
        {
            printf("===> WriteFile is Failed!\n");

            return -1;
        }

        printf("===> convertAfterBuf: \n%s\n\n", convertAfterBuf);

        //判断是否已到文件尾
        if ((MAXLEN-1) > strlen(convertBeforeBuf))
        {
            printf("===> Congratulations！Read File Over!\n");

            break;
        }
    }

    fclose(fpRead);
    return 0;
}



JNIEXPORT void JNICALL Java_com_yt_reader_optionmenu_TextSearch_iconv
(JNIEnv *env, jobject obj, jstring jfrom, jstring jto, jstring jfpath, jstring jtpath)
{

//	LOGI("open iconv success!");
	char* from = (*env)->GetStringUTFChars(env, jfrom, NULL);
	char* to = (*env)->GetStringUTFChars(env, jto, NULL);
	char* fpath = (*env)->GetStringUTFChars(env, jfpath, NULL);
	char* tpath = (*env)->GetStringUTFChars(env, jtpath, NULL);
	myconvert(from, to, fpath, tpath);

}

