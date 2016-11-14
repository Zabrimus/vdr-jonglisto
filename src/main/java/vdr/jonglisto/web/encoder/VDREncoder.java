package vdr.jonglisto.web.encoder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.services.ValueEncoderFactory;

import vdr.jonglisto.lib.model.VDR;

public class VDREncoder implements ValueEncoder<VDR>, ValueEncoderFactory<VDR> {

    private Map<String, VDR> vdrs;

    public VDREncoder() {
        vdrs = new HashMap<>();
    }

    public VDREncoder(List<VDR> ch) {
        vdrs = new HashMap<>();
        addVdrs(ch);
    }

    @Override
    public String toClient(VDR value) {
        return value.getUuid();
    }

    @Override
    public VDR toValue(String id) {
        return vdrs.get(id);
    }

    @Override
    public ValueEncoder<VDR> create(Class<VDR> type) {
        return this;
    }

    public void addVdrs(List<VDR> ch) {
        ch.stream().forEach(s -> vdrs.put(s.getUuid(), s));
    }
}