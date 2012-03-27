package org.hypergraphdb.storage.redis;
import org.hypergraphdb.HGException;
import org.hypergraphdb.HGRandomAccessResult;
import org.hypergraphdb.HGSearchResult;
import org.hypergraphdb.HGSortIndex;
import org.hypergraphdb.storage.ByteArrayConverter;
import org.hypergraphdb.storage.HGConverter;
import org.hypergraphdb.transaction.HGTransactionManager;
import org.hypergraphdb.util.HGUtils;
import redis.clients.jedis.BinaryTransaction;
import redis.clients.jedis.Jedis;

//import java.util.*;


import java.util.*;

/**
 * <p>
 * The default jedis-based index implementation. This implementation works by maintaining
 * a separate sub-database (redis select) for each index.
 * </p>
 * @author Ingvar Bogdahn
 */

public class JedisIndex<KeyType, ValueType> implements HGSortIndex<KeyType, ValueType> {

    //Prefix of HyperGraph index DB filenames.
    public static final String DB_NAME_PREFIX = "hgstore_idx_";


    protected Map<Integer, byte[]> indexKeySet = new LinkedHashMap<Integer, byte[]>();
    protected JedisStore storage;    //
    protected HGTransactionManager transactionManager;
    protected int jedisDbId;                        // this is the # of the jedis database used for this index.
    protected String name;
    protected ByteArrayConverter<KeyType> keyConverter;
    protected ByteArrayConverter<ValueType> valueConverter;
    protected Comparator<?> comparator;

    protected boolean useCache;

    public JedisIndex(String indExName,
                      JedisStore stOrage,
                      HGTransactionManager trAnsactionManager,
                      ByteArrayConverter<KeyType> kEyConverter,
                      ByteArrayConverter<ValueType> vAlueConverter,
                      Comparator cOmparator) {
        this.name = indExName;
        this.jedisDbId = stOrage.lookupIndexIdElseCreate(DB_NAME_PREFIX + name);
        this.storage = stOrage;
        this.transactionManager = trAnsactionManager;
        this.keyConverter = kEyConverter;
        this.valueConverter = vAlueConverter;
        this.comparator = cOmparator;
    }

    public void open() {     }
    public void close() {    }
    public boolean isOpen() { return true;  }

    protected void updateIndex() {   jedisDbId = storage.lookupIndexIdElseCreate(DB_NAME_PREFIX + name);    }

    protected void updateKeySetAndIndexID() { updateKeySetAndIndexID(storage.keys(jedisDbId, "*".getBytes()));}
    protected void updateKeySetAndIndexID(Set<byte[]> jedisKeySet) {
        updateIndex();
        int tempHash;
        for(byte[] ba : jedisKeySet)   {
            tempHash = Arrays.hashCode(ba);
            if (!indexKeySet.containsKey(tempHash))
                indexKeySet.put(tempHash, ba);
        }
    }


    public HGRandomAccessResult<KeyType> scanKeys() {
        HGRandomAccessResult result = null;
        Set<byte[]> jks = storage.keys(jedisDbId, "*".getBytes());
        try {
            updateKeySetAndIndexID(jks);
            if (!indexKeySet.isEmpty())
                result = new JKeyResultSet<KeyType>(storage, jedisDbId, jks, keyConverter, useCache);
            else
                result = HGSearchResult.EMPTY;
        }
        catch (Throwable ex) {  throw new HGException("Failed to lookup index '" +  name + "': " + ex.toString(),  ex); }
        return result;
    }


    public HGRandomAccessResult<ValueType> scanValues() {

        HGRandomAccessResult result = HGSearchResult.EMPTY;
        try {
            Set<byte[]> jks = storage.keys(jedisDbId, "*".getBytes());
            updateKeySetAndIndexID(jks);
            if (!indexKeySet.isEmpty())
            {
                byte[] keySetContext = HGConverter.concat("scanValues".getBytes(), HGConverter.intToByteArray(jedisDbId));
                result = new JValueRSOverMultiKeys<ValueType>(storage, jedisDbId, jks, valueConverter, keySetContext, false, useCache);
            }
        }
        catch (Throwable ex) { throw new HGException("Failed to lookup index '" +  name + "': " + ex.toString(),  ex); }
        return result;
    }

    public void addEntry(KeyType key, ValueType value) {
        Jedis master = null;
        byte[] keyBA = keyConverter.toByteArray(key);
        try
        {
            updateIndex();  //
            master = storage.getWritePool().getResource();
            master.watch(keyBA);
            BinaryTransaction bt = master.multi();
            bt.select(jedisDbId);
            bt.zadd(keyBA, 0, valueConverter.toByteArray(value));
            bt.select(0);
            bt.zadd(storage.keySet(jedisDbId), 0, keyBA);
            bt.exec();
        }
        catch (Exception ex) {  throw new HGException("Failed to add entry to index '" +  name + "': " + ex.toString(), ex);  }
        finally {  storage.returnWriteJedis(master);  }
        indexKeySet.put(Integer.valueOf(Arrays.hashCode(keyBA)), keyBA);
    }

    public void removeEntry(KeyType key, ValueType value) {
        Jedis master = null;
        byte[] keyBA = keyConverter.toByteArray(key);
        try
        {
            updateIndex();
            master = storage.getWritePool().getResource();
            master.watch(keyBA);
            BinaryTransaction bt = master.multi();
            bt.select(jedisDbId);
            bt.zrem(keyBA, valueConverter.toByteArray(value));
            bt.select(0);
            bt.zrem(storage.keySet(jedisDbId), keyBA);
            bt.exec();
        }
        catch (Exception ex) {  throw new HGException("Failed to delete entry from index '" +  name + "': " + ex.toString(),  ex); }
        finally {  storage.returnWriteJedis(master);  }
        indexKeySet.remove(Integer.valueOf(Arrays.hashCode(keyBA)));
    }

    public void removeAllEntries(KeyType key) {
        Jedis master = null;
        try
        {
            byte[] keyBA = keyConverter.toByteArray(key);

            updateIndex();
            master = storage.getWritePool().getResource();
            master.watch(keyBA);
            BinaryTransaction bt = master.multi();
            bt.select(jedisDbId);
            bt.zremrangeByRank(keyBA, 0, -1);
            bt.exec();
        }
        catch (Exception ex) {  throw new HGException("Failed to delete all entries from index '" +  name + "': " + ex.toString(),  ex); }
        finally {  storage.returnWriteJedis(master);  }
    }

    public ValueType findFirst(KeyType key)
    {
        ValueType result;
        updateKeySetAndIndexID();
        byte[] temp =null;
        try
        {
            temp= storage.zrangeAt(jedisDbId, keyConverter.toByteArray(key), 0);
        }

        catch (Exception ex) { throw new HGException("Failed to find first of key '" + key.toString() + "': " + ex.toString(),ex); }
        if(temp ==null)
            result = null;
        else
            result = valueConverter.fromByteArray(temp);
        return result;
    }

    public ValueType findLast(KeyType key) {
        ValueType result;
        updateKeySetAndIndexID();
        try
        {
            result = valueConverter.fromByteArray(storage.zrangeAt(jedisDbId, keyConverter.toByteArray(key), -1));
        }
        catch (Exception ex) { throw new HGException("Failed to find Last of key'" +  key.toString() + "': " + ex.toString(),  ex); }
        return result;
    }

    public HGRandomAccessResult<ValueType> find(KeyType key) {
        byte[] keyBA = keyConverter.toByteArray(key);
        HGRandomAccessResult result;
        updateKeySetAndIndexID();
        try {
            int zcard = storage.zcard(jedisDbId, keyBA).intValue();

            if (zcard > 0)
                result = new JValueRSOverSingleKey(jedisDbId, keyBA, storage, valueConverter, false);
            else
                result = HGSearchResult.EMPTY;
        }
        catch (Exception ex) { throw new HGException("Failed to find resultset of key" + key.toString() + "': " + ex.toString(), ex); }
        return result;
    }

   public HGSearchResult<ValueType> findXY(KeyType key, boolean downwards, boolean equal) {
        HGSearchResult<ValueType> result = null;
        byte[] keyBA = keyConverter.toByteArray(key);
        updateKeySetAndIndexID();

        boolean isContained = storage.zrank(0, storage.keySet(jedisDbId), keyBA) != null;
        List<byte[]> allKeysBytAr = new ArrayList<byte[]>(storage.zrange(0, storage.keySet(jedisDbId), 0, -1));
        Comparator<byte[]> compArator = null;

       if(this.comparator == null)
            compArator = new ByteArrayComparator();
       else
            compArator = (Comparator<byte[]>) this.comparator;

       if(!isContained)
            allKeysBytAr.add(keyBA);

       Collections.sort(allKeysBytAr, compArator);
       int rank = Collections.binarySearch(allKeysBytAr, keyBA, (Comparator<byte[]>) compArator);
        Set<byte[]> resultKeySet = new LinkedHashSet<byte[]>(); 

        try
        {
        
            if(equal && downwards)
            {
                resultKeySet.addAll(allKeysBytAr.subList(0, rank + 1));
                if (resultKeySet.size() == 0)  result = (HGSearchResult<ValueType>) HGSearchResult.EMPTY;
                else
                {
                    byte[] keySetContext = HGConverter.concat("findLTE".getBytes(), keyConverter.toByteArray(key));
                    result = new JValueRSOverMultiKeys(storage, jedisDbId, resultKeySet, valueConverter, keySetContext, downwards, useCache);
                }
            }

            if(!equal && downwards)
            {
                resultKeySet.addAll(allKeysBytAr.subList(0, rank));
                if (resultKeySet.size() == 0) result = (HGSearchResult<ValueType>) HGSearchResult.EMPTY;
                else
                {
                    byte[] keySetContext = HGConverter.concat("findLT".getBytes(), keyConverter.toByteArray(key));
                    result = new JValueRSOverMultiKeys(storage, jedisDbId, resultKeySet, valueConverter, keySetContext, downwards, useCache);
                }
            }

            if(equal && !downwards)
            {
                resultKeySet.addAll(allKeysBytAr.subList(rank, allKeysBytAr.size()));
                if (resultKeySet.size() == 0)  result = (HGSearchResult<ValueType>) HGSearchResult.EMPTY;
                else
                {
                    byte[] keySetContext = HGConverter.concat("findGTE".getBytes(), keyConverter.toByteArray(key));
                    result = new JValueRSOverMultiKeys(storage, jedisDbId, resultKeySet, valueConverter, keySetContext, downwards, useCache);
                }
            }

            if(!equal && !downwards)
            {
                resultKeySet.addAll(allKeysBytAr.subList(rank + 1, allKeysBytAr.size()));
                if (resultKeySet.size() == 0)  result = (HGSearchResult<ValueType>) HGSearchResult.EMPTY;
                else
                {
                    byte[] keySetContext = HGConverter.concat("findGT".getBytes(), keyConverter.toByteArray(key));
                    result = new JValueRSOverMultiKeys(storage, jedisDbId, resultKeySet, valueConverter, keySetContext, downwards, useCache);
                }
            }
        }
        catch (Throwable t) { throw new HGException(t); }
        return result;
    }

    public HGSearchResult<ValueType> findGT(KeyType key)    {   return findXY(key, false, false);   }
    public HGSearchResult<ValueType> findGTE(KeyType key)   {   return findXY(key, false, true);    }
    public HGSearchResult<ValueType> findLT(KeyType key)    {   return findXY(key, true, false);    }
    public HGSearchResult<ValueType> findLTE(KeyType key)   {   return findXY(key, true, true);     }

    protected void finalize() { }

    public long count() {
        updateKeySetAndIndexID();
        return indexKeySet.size();
    }

    public long count(KeyType key) {
        updateKeySetAndIndexID();
        long keyMembers;
        keyMembers = storage.zcard(jedisDbId, keyConverter.toByteArray(key));
        return keyMembers;
    }

    public String getName() { return name; }
    public String getDatabaseName() { return DB_NAME_PREFIX + name;  }
}