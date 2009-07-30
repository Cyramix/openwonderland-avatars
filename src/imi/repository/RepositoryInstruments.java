/**
 * Project Wonderland
 *
 * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
 *
 * Redistributions in source code form must reproduce the above
 * copyright and this condition.
 *
 * The contents of this file are subject to the GNU General Public
 * License, Version 2 (the "License"); you may not use this file
 * except in compliance with the License. A copy of the License is
 * available at http://www.opensource.org/licenses/gpl-license.php.
 *
 * Sun designates this particular file as subject to the "Classpath"
 * exception as provided by Sun in the License file that accompanied
 * this code.
 */
package imi.repository;

import java.util.List;
import java.util.Map;
import javolution.util.FastList;
import javolution.util.FastMap;

/**
 * Instrumentation object for use with the repository.
 * @author Ronald E Dahlgren
 */
public class RepositoryInstruments {
    /** indicates that the repository being monitored is using the cache **/
    private boolean usingCache;
    /** Keeps track of request times **/
    private final Map<Object, SimpleTimer> requestTimerMap = new FastMap();
    
    private final List<Float> requestTimes = new FastList<Float>();

    private float totalTimeInRequests;
    private int totalReceivedRequests;
    private int totalCompletedRequests;

    /**
     * Construct a new RepositoryInstruments.
     * @param usingCache True if the monitored repo is using a caching behavior
     */
    public RepositoryInstruments(boolean usingCache)
    {
        this.usingCache = usingCache;
    }

    /**
     * Signal that an asset request has been started for the supplied asset.
     * @param asset The asset being requested
     */
    synchronized void requestStarted(SharedAsset asset)
    {
        requestTimerMap.put(asset, getTimer().start());
        totalReceivedRequests++;
    }

    /**
     * Signal that a request has completed for the supplied asset.
     * @param asset The asset that has been loaded
     * @throws NullPointerException If the asset was never requested
     */
    synchronized void requestComplete(SharedAsset asset)
    {
        SimpleTimer timer = requestTimerMap.remove(asset).stop();

        float time = timer.totalTimeInSeconds();
        totalTimeInRequests += time;
        requestTimes.set(timer.id, Float.valueOf(time));
        totalCompletedRequests++;
    }

    /**
     * Dump the statistics to the system console.
     */
    synchronized void dumpStats()
    {
        System.out.println("Cache: " + usingCache);
        System.out.println("Total Requests: " + totalCompletedRequests);
        System.out.println("Time spent in requests: " + totalTimeInRequests);
        if (totalCompletedRequests > 0)
            System.out.println("Average: " + (totalTimeInRequests / (float)totalCompletedRequests));
    }

    /**
     * Used to track what the next timer id will be.
     */
    private int nextTimerID = 0;

    /**
     * Factory method for creating timers
     * @return
     */
    private SimpleTimer getTimer()
    {
        SimpleTimer result = new SimpleTimer(nextTimerID);
        nextTimerID++;
        return result;
    }

    /**
     * Retrieve a list of the load times, normalized to be integers between
     * zero and <code>maxValue</code>.
     * <p>
     * The values are calculated and then added into the valuesOut collection.
     * This method is an N^2 operation. It is not a lightweight operation.
     * Which way does the algorithm round?
     * </p>
     * @param maxValue The maximum value that may be represented
     * @param valuesOut A non-null list to store the results
     *
     * @throws IllegalArgumentException If maxValue &lt;= 0
     * @throws NullPointerException if valuesOut == null
     */
    synchronized void getNormalizedListOfTimes(int maxValue, List<Integer> valuesOut)
    {
        if (maxValue <= 0)
            throw new IllegalArgumentException("maxValue <= 0; maxValue was " + maxValue);
        // Determine longest load time
        Float max = Float.valueOf(0.0f);
        for (Float f : requestTimes)
            if (f.compareTo(max) > 0)
                max = f;
        // Determine size of time deltas between values
        Float delta = Float.valueOf(max / (float)maxValue);
        // iterate and convert
        for (Float f : requestTimes)
            valuesOut.add(Integer.valueOf((int)(f / delta)));
    }

    /**
     * Performs rudimentary timing operations and maintains an id.
     */
    private class SimpleTimer
    {

        long totalTimeMillis;
        long startTimeMillis;
        long endTimeMillis;
        int id;

        SimpleTimer(int id) {
            this.id = id;
        }
        
        SimpleTimer start()
        {
            startTimeMillis = System.currentTimeMillis();
            return this;
        }

        SimpleTimer stop()
        {
            endTimeMillis = System.currentTimeMillis();
            totalTimeMillis += endTimeMillis - startTimeMillis;
            return this;
        }

        float totalTimeInSeconds()
        {
            return (totalTimeMillis / 1000.0f);
        }
    }

}
