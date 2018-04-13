package com.exqudens.hibernate.util;

import java.lang.reflect.Field;

import org.hibernate.action.internal.AbstractEntityInsertAction;
import org.hibernate.action.spi.Executable;
import org.hibernate.engine.spi.ActionQueue;
import org.hibernate.engine.spi.ExecutableList;
import org.hibernate.engine.spi.SessionImplementor;

public class TmpUtils {

    public static String getInsertions(SessionImplementor session) {
        try {
            String result = "";
            for (Field field : ActionQueue.class.getDeclaredFields()) {
                if ("insertions".equals(field.getName())) {
                    ActionQueue actionQueue = session.getActionQueue();
                    boolean accessible = field.isAccessible();
                    field.setAccessible(true);
                    Object insertionsObject = field.get(actionQueue);
                    field.setAccessible(accessible);
                    if (insertionsObject != null) {
                        ExecutableList<?> executableList = ExecutableList.class.cast(insertionsObject);
                        for (Executable executable : executableList) {
                            AbstractEntityInsertAction insertAction = AbstractEntityInsertAction.class.cast(executable);
                            result += insertAction.getEntityName() + System.lineSeparator();
                        }
                    }
                }
            }
            return result;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private TmpUtils() {
        super();
    }

}
