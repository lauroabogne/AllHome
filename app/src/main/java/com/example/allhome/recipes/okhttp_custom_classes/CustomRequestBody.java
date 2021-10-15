package com.example.allhome.recipes.okhttp_custom_classes;


import android.util.Log;

import java.io.IOException;

import androidx.annotation.Nullable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

public class CustomRequestBody extends RequestBody {


    RequestBody mDelegateRequestBody;
    OkHttpUploadProgressListener mProgressListener;
    CountingSink mCountingSink;

    public CustomRequestBody(RequestBody delegateRequestBody, OkHttpUploadProgressListener progressListener) throws IOException {

        super();
        mDelegateRequestBody = delegateRequestBody;
        mProgressListener = progressListener;

        Log.e("test123","test");
    }
    @Nullable
    @Override
    public MediaType contentType() {
        return mDelegateRequestBody.contentType();
    }

    @Override
    public long contentLength(){


        try {
            return mDelegateRequestBody.contentLength();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    BufferedSink bufferedSink;

    @Override
    public void writeTo(BufferedSink sink) throws IOException {


        mCountingSink = new CountingSink(sink);


        bufferedSink= Okio.buffer(mCountingSink);
        mDelegateRequestBody.writeTo(bufferedSink);

        bufferedSink.flush();


    }


    protected final class CountingSink extends ForwardingSink {

        long mContentLenght = 0;
        private long bytesWritten = 0;

        public CountingSink(Sink delegate) throws IOException {
            super(delegate);

            mContentLenght = contentLength();
        }
        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);


            bytesWritten += byteCount;

            Log.e("size_x_test",bytesWritten+" "+mContentLenght);
            if(mProgressListener !=null){

                mProgressListener.onProgress(byteCount,mContentLenght);


            }





        }
    }
    public interface OkHttpUploadProgressListener{

        void onProgress(long uploaded, long size);
    }
}
