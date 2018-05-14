package com.exqudens.hibernate.listener;

import java.util.List;

import org.hibernate.event.spi.EventSource;

interface SelfCacheEventListener {

    List<Object> remove(EventSource eventSource);

}
