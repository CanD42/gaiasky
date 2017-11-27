package gaia.cu9.ari.gaiaorbit.client.format;

import com.google.gwt.i18n.client.NumberFormat;

import gaia.cu9.ari.gaiaorbit.util.format.INumberFormat;

public class GwtNumberFormat implements INumberFormat {
    private final NumberFormat nf;

    public GwtNumberFormat(String pattern) {
        nf = NumberFormat.getFormat(pattern);
    }

    @Override
    public String format(double num) {
        return nf.format(num);
    }

    @Override
    public String format(long num) {
        return nf.format(num);
    }

}
