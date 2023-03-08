package com.example.calculatorconsum.WebScraping;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class GetHTMLTask extends AsyncTask<String, Void, Document> {

    private Callback callback;

    public GetHTMLTask(Callback callback) {
        this.callback = callback;
    }

    @Override
    protected Document doInBackground(String... urls) {
        try {
            return Jsoup.connect(urls[0]).get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(Document document) {
        super.onPostExecute(document);
        if (callback != null) {
            callback.onHtmlLoaded(document);
        }
    }

    public interface Callback {
        void onHtmlLoaded(Document document);
    }
}
