package cn.neyzoter.aiot.fddp.biz.service.tensorflow;

import cn.neyzoter.aiot.common.tensorflow.ModelManager;
import cn.neyzoter.aiot.common.util.PropertiesUtil;
import cn.neyzoter.aiot.fddp.biz.service.bean.PropertiesLables;
import cn.neyzoter.aiot.fddp.biz.service.bean.PropertiesValueRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * vehicle model table
 * @author Neyzoter Song
 * @date 2020-3-7
 */
@Component
public class VehicleModelTable implements Serializable {
    private static final long serialVersionUID = 8236013173494095071L;

    public static final Logger logger = LoggerFactory.getLogger(VehicleModelTable.class);
    private Map<String, ModelManager> modelMap;
    private PropertiesUtil propertiesUtil;

    @Autowired
    public VehicleModelTable (PropertiesUtil p) {
        modelMap = new ConcurrentHashMap<>();
        this.propertiesUtil = p;
    }

    /**
     * get a model manager
     * @param key key
     * @return ModelManager {@link ModelManager}
     */
    public ModelManager getModelManager(String key) {
        return modelMap.get(key);
    }

    /**
     * put new model manager into hash
     * @param key key
     * @param manager value - manager
     */
    public void putModelManager(String key, ModelManager manager) {
        modelMap.put(key, manager);
    }

    /**
     * check all model manager is time out or not, if time out , remove it
     * do not use maxAliveTime of {@link ModelManager}, but use the maxAliveTime in properties file
     * @return {@link List} removed managers' key list
     */
    public List<String> aliveCheck () {
        Iterator<Map.Entry<String, ModelManager>> iter = modelMap.entrySet().iterator();
        List<String> rmList = new LinkedList<>();
        int maxAliveTime = getMaxAliveTime();
        for (;iter.hasNext();) {
            ModelManager manager = iter.next().getValue();
            // if time out
            if (manager.isTimeout(maxAliveTime)) {
                String key = iter.next().getKey();
                rmList.add(key);
                // remove the model manager
                modelMap.remove(key);
            }
        }
        return rmList;
    }

    /**
     * inc all model manager and check, if timeout then rm<br/>
     * do not use maxAliveTime of {@link ModelManager}, but use the maxAliveTime in properties file
     * @return {@link List} removed managers' key list
     */
    public List<String> aliveIncCheck () {
        Iterator<Map.Entry<String, ModelManager>> iter = modelMap.entrySet().iterator();
        List<String> rmList = new LinkedList<>();
        int maxAliveTime = getMaxAliveTime();
        int incTime = getPeriod();
        for (;iter.hasNext();) {
            ModelManager manager = iter.next().getValue();
            manager.aliveTimeInc();
            // if time out
            if (manager.isTimeout(maxAliveTime)) {
                String key = iter.next().getKey();
                rmList.add(key);
                // remove the model manager
                modelMap.remove(key);
            }
        }
        return rmList;
    }

    /**
     * refresh alive time, if alive time exceed the max alive time , this k-v will be GC
     * @param key key
     */
    public void resetAliveTime (String key) {
        modelMap.get(key).setAliveTime(0);
    }

    /**
     * set max alive time
     * @param key key - vtype
     * @param time time , unit is same with maxAliveTime of {@link ModelManager}
     */
    public void setMaxAliveTime (String key , int time) {
        modelMap.get(key).setMaxAliveTime(time);
    }

    /**
     * put a model manager into map
     * @param key key - vtype
     * @param path path of model
     * @param tag tag of model
     * @param time max alive time, if alive time bigger than time, the model will be GC
     */
    public void put (String key, String path, String tag, int time) {
        try {
            logger.info(String.format("Start loading model: %s  tag: %s", path, tag));
            long startime = System.currentTimeMillis();
            ModelManager modelManager = new ModelManager(path, tag, time);
            modelMap.put(key, modelManager);
            logger.info(String.format("Loaded model: %s  tag: %s finished in %d ms", path, tag, System.currentTimeMillis() - startime));
        } catch (Exception e) {
            logger.error("",e);
        }

    }

    /**
     * put a model manager into map with a default max alive time
     * @param key key - vtype
     * @param path path of model
     * @param tag tag of model
     */
    public void put (String key, String path, String tag) {
        this.put(key, path, tag, Integer.MAX_VALUE);
    }

    /**
     * get model path
     * @param propertiesUtil properties util
     * @return model path
     */
    public String getModelPath (PropertiesUtil propertiesUtil) {
        String path = propertiesUtil.readValue(PropertiesLables.TENSORFLOW_MODEL_PATH);
        return path;
    }

    /**
     * get time unit from properties file
     * @return {@link TimeUnit}
     */
    public TimeUnit getTimeUnit () {
        String unit = this.propertiesUtil.readValue(PropertiesLables.THREADPOOL_SCHEDULED_EXECUTOR_TF_MODEL_CHECK_UNIT);
        unit = unit.trim();
        switch (unit) {
            case PropertiesValueRange.UNIT_HOUR:
                return TimeUnit.HOURS;
            case PropertiesValueRange.UNIT_MINUTTE:
                return TimeUnit.MINUTES;
            case PropertiesValueRange.UNIT_SECOND:
                return TimeUnit.SECONDS;
            default:
                logger.warn("Period Unit Unrecognized， set as default SECONDS");
                return TimeUnit.SECONDS;

        }
    }

    /**
     * get period
     * @return period
     */
    public int getPeriod () {
        String period = this.propertiesUtil.readValue(PropertiesLables.THREADPOOL_SCHEDULED_EXECUTOR_TF_MODEL_CHECK_PERIOD);
        return Integer.parseInt(period);
    }

    /**
     * get max alive time
     * @return maxAliveTime
     */
    public int getMaxAliveTime () {
        String period = this.propertiesUtil.readValue(PropertiesLables.TENSORFLOW_MODEL_MAX_ALIVE_TIME);
        return Integer.parseInt(period);
    }
}
