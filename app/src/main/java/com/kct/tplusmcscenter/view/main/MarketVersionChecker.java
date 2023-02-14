package com.kct.tplusmcscenter.view.main;

import android.util.Log;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MarketVersionChecker {

    public static String getMarketVersion(String packageName) {
        try {
            Log.d("MarketVersionChecker", "url : https://play.google.com/store/apps/details?id=" + packageName);
            Document doc = Jsoup.connect( "https://play.google.com/store/apps/details?id="  + packageName).get();

            Log.d("MarketVersionChecker", "doc 오나 " );
            Log.d("MarketVersionChecker", "doc " + doc);

            Elements Version = doc.select(".content");

            Log.d("MarketVersionChecker", "doc Version " + Version);

            for (Element mElement : Version) {
                Log.d("MarketVersionChecker", "mElement.attr(itemprop)= " + mElement.attr("itemprop"));
                Log.d("MarketVersionChecker", "mElement.text = " + mElement.attr(mElement.text().trim()));

                if (mElement.attr("itemprop").equals("softwareVersion")) {
                    Log.d("MarketVersionChecker", "mElement.text().trim()= " + mElement.text().trim());
                    return mElement.text().trim();
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
            Log.d("VIRSION ERROR2", ex.getMessage());
        }

        return null;
    }

    public static String getMarketVersionFast(String packageName) {
        String mData = "", mVer = null;

        try {
            URL mUrl = new URL("https://play.google.com/store/apps/details?id="  + packageName);
            HttpURLConnection mConnection = (HttpURLConnection) mUrl  .openConnection();

            if (mConnection == null)
                return null;

            mConnection.setConnectTimeout(5000);
            mConnection.setUseCaches(false);
            mConnection.setDoOutput(true);

            if (mConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader mReader = new BufferedReader(
                        new InputStreamReader(mConnection.getInputStream()));

                while (true) {
                    String line = mReader.readLine();
                    if (line == null)
                        break;
                    mData += line;
                }

                mReader.close();
            }

            mConnection.disconnect();

        } catch (Exception ex) {
            ex.printStackTrace();
            Log.d("VIRSION ERROR3", ex.getMessage());
            return null;
        }

        String startToken = "softwareVersion\">";
        String endToken = "<";
        int index = mData.indexOf(startToken);

        if (index == -1) {
            mVer = null;

        } else {
            mVer = mData.substring(index + startToken.length(), index
                    + startToken.length() + 100);
            mVer = mVer.substring(0, mVer.indexOf(endToken)).trim();
        }

        return mVer;
    }
}
