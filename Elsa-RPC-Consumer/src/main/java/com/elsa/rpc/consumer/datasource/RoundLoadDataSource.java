package com.elsa.rpc.consumer.datasource;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName : RoundLoadDataSource  //类名
 * @Author : elsa //作者
 */
public class RoundLoadDataSource extends LoadDataSouce{
    private AtomicInteger atomicInteger = new AtomicInteger(0);

    public RoundLoadDataSource(String serviceName) {
        super(serviceName);
    }

    @Override
    public Connection pickOne() {
        int andIncrement = atomicInteger.incrementAndGet();
        if (andIncrement <= 0) {
            synchronized (atomicInteger) {
                if (atomicInteger.get() <= 0) {
                    atomicInteger.set(0);
                }
            }
            andIncrement = atomicInteger.incrementAndGet();
        }
        try{
            int index = andIncrement % connections().size();
            Connection connectionInfo = connections().get(index);
            return connectionInfo;
        }catch (IndexOutOfBoundsException e){
            e.printStackTrace();
            return pickOne();
        }
    }
}
