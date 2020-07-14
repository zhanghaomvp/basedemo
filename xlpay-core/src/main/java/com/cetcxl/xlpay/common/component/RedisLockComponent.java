package com.cetcxl.xlpay.common.component;

import com.cetcxl.xlpay.common.exception.BaseRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.cetcxl.xlpay.common.component.ApplicationContextComponet.APPLICATION_CONTEXT;
import static com.cetcxl.xlpay.common.constants.CommonResultCode.REDIS_LOCK_ERROR;

@Component
public class RedisLockComponent {
    @Autowired
    private RedisTemplate redisTemplate;

    private final static String KEY_PREFIX = "redis.global.lock.";
    private final static String LOCKED = "lock";

    private final long timeout = 3 * 1000L;
    private final long expireMillis = 3 * 60 * 1000L;

    public boolean lock(String key) {
        return lock(KEY_PREFIX + key, expireMillis);
    }

    public boolean unlock(String key) {
        return redisTemplate.delete(KEY_PREFIX + key);
    }

    private boolean lock(String key, long expireMillis) {
        long mills = System.currentTimeMillis();

        while ((System.currentTimeMillis() - mills) < timeout) {
            Boolean isLocked = (Boolean) redisTemplate.execute(
                    (RedisCallback<Boolean>) connection -> {
                        RedisStringCommands.SetOption setOption = RedisStringCommands.SetOption.ifAbsent();
                        Expiration expiration = Expiration.milliseconds(expireMillis);
                        Boolean set = connection.set(
                                key.getBytes(),
                                LOCKED.getBytes(),
                                expiration,
                                setOption
                        );
                        return set;
                    }
            );

            if (isLocked) {
                return true;
            }
        }

        return false;
    }

    public static class RedisLock implements AutoCloseable {
        private static ThreadLocal<List<String>> KEYS = new ThreadLocal<List<String>>();
        private static ThreadLocal<String> KEY = new ThreadLocal<String>();

        private RedisLockComponent redisLockComponent;
        private boolean isMultiple;

        public RedisLock(String key) {
            isMultiple = false;
            redisLockComponent = APPLICATION_CONTEXT
                    .getBean(RedisLockComponent.class);

            KEY.set(key);
            if (!redisLockComponent.lock(key)) {
                throw new BaseRuntimeException(REDIS_LOCK_ERROR);
            }
        }

        public RedisLock(List<String> keys) {
            isMultiple = true;
            redisLockComponent = APPLICATION_CONTEXT
                    .getBean(RedisLockComponent.class);

            KEYS.set(keys);
            keys
                    .forEach(key ->
                            {
                                if (!redisLockComponent.lock(key)) {
                                    throw new BaseRuntimeException(REDIS_LOCK_ERROR);
                                }

                            }
                    );

        }

        @Override
        public void close() {
            if (isMultiple) {
                KEYS.get()
                        .forEach(
                                key -> redisLockComponent.unlock(key)
                        );
            } else {
                redisLockComponent.unlock(KEY.get());
            }
        }
    }
}
