package cn.neyzoter.aiot.iov.biz.domain.vehicle;

import cn.neyzoter.aiot.common.data.serialization.SerializationUtil;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

/**
 * VehicleHttpPack Serializer
 * @author Neyzoter Song
 * @date 2019/9/11
 */
public class VehicleHttpPackSerializer implements Serializer<VehicleHttpPack> {
    private final static Logger logger = LoggerFactory.getLogger(VehicleHttpPackSerializer.class);
    @Override
    public void configure(Map<String, ?> config, boolean isKey){
        // do nothing
    }
    @Override
    public byte[] serialize(String topic, VehicleHttpPack vehicleHttpPack){
        try{
            return SerializationUtil.serialize(vehicleHttpPack);
        }catch (Exception e){
            logger.error("",e);
        }
        return null;
    }
    @Override
    public void close() {
        //do nothing
    }
}
