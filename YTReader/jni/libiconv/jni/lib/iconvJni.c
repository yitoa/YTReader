#include <string.h>
#include <jni.h>
#include <dlfcn.h>
#include <android/log.h>
#include <stdlib.h>
#include <stdio.h>
#include <locale.h>

#include <iconv.h>
#define BUFFER_SIZE 1024

#define  LOG_TAG    "iconvJni"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

JNIEXPORT void JNICALL Java_com_yt_reader_optionmenu_TextSearch_iconv
(JNIEnv *env, jobject obj, jstring jfrom, jstring jto, jstring jfpath, jstring jtpath)
{

	LOGI("open iconv success!");
	char* from = (*env)->GetStringUTFChars(env, jfrom, NULL);
	char* to = (*env)->GetStringUTFChars(env, jto, NULL);
	char* fpath = (*env)->GetStringUTFChars(env, jfpath, NULL);
	char* tpath = (*env)->GetStringUTFChars(env, jtpath, NULL);
	convert(from, to, fpath, tpath);

}

int convert(char* from, char* to, char* fpath, char* tpath)
{
    FILE * pSrcFile = NULL;
	FILE * pDstFile = NULL;
    char szSrcBuf[BUFFER_SIZE];
    char szDstBuf[BUFFER_SIZE];
    size_t nSrc = 0;
    size_t nDst = 0;
    size_t nRead = 0;
    size_t nRet = 0;
    char *pSrcBuf = szSrcBuf;
    char *pDstBuf = szDstBuf;
    iconv_t icv;
    int argument = 1;

    pSrcFile = fopen(fpath,"r");
    if(pSrcFile == NULL)
    {
        LOGI("can't open source file!\n");
        return -1;
    }
    pDstFile = fopen(tpath,"w");
    if(pSrcFile == NULL)
    {
        LOGI("can't open destination file!\n");
        return -1;
    }
    icv = iconv_open(to, from);
    if(icv == 0)
    {
        LOGI("can't initalize iconv routine!\n");
        return -1;
    }
    //enable "illegal sequence discard and continue" feature, so that if met illeagal sequence,
    //conversion will continue instead of being terminated
    if(iconvctl (icv ,ICONV_SET_DISCARD_ILSEQ,&argument) != 0)
    {
        LOGI("can't enable \"illegal sequence discard and continue\" feature!\n");
        return -1;
    }
    while(!feof(pSrcFile))
    {
        pSrcBuf = szSrcBuf;
        pDstBuf = szDstBuf;
        nDst = BUFFER_SIZE;
        // read data from source file
        nRead = fread(szSrcBuf + nSrc,sizeof(char),BUFFER_SIZE - nSrc,pSrcFile);
        if(nRead == 0)
            break;
        // the amount of data to be converted should include previous left data and current read data
        nSrc = nSrc + nRead;
        //perform conversion
        nRet = iconv(icv,(char**)&pSrcBuf,&nSrc,&pDstBuf,&nDst);
        if(nRet == -1)
        {
            // include all case of errno: E2BIG, EILSEQ, EINVAL
            //     E2BIG: There is not sufficient room at *outbuf.
            //     EILSEQ: An invalid multibyte sequence has been encountered in the input.
            //     EINVAL: An incomplete multibyte sequence has been encountered in the input
            // move the left data to the head of szSrcBuf in other to link it with the next data block
            memmove(szSrcBuf,pSrcBuf,nSrc);
        }
       //wirte data to destination file
        fwrite(szDstBuf,sizeof(char),BUFFER_SIZE - nDst,pDstFile);

    }

    iconv_close(icv);
    fclose(pSrcFile);
    fclose(pDstFile);
    LOGI("conversion complete.\n");
    return 0;
}
