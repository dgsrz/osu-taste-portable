package com.dgsrz.osuTaste.services;

/**
 * @author dgsrz (dgsrz@vip.qq.com)
 */
public interface BassInterface {
    public void onPluginsLoaded(String plugins);
    public void onFileLoaded(String file, double duration, String info);
    public void onProgressChanged(double progress);
}