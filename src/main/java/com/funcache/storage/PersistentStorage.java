package com.funcache.storage;

import java.util.List;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface PersistentStorage {

    boolean saveAll(List<Object> data);

    boolean save(Object data);

    boolean contains(Object data);
}
