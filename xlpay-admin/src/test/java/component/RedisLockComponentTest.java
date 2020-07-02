package component;

import com.cetcxl.xlpay.BaseTest;
import com.cetcxl.xlpay.common.component.RedisLockComponent;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RedisLockComponentTest extends BaseTest {
    @Autowired
    RedisLockComponent redisLockComponent;

    @Test
    @Disabled
    void redis_lock_unlock() {
        assertTrue(redisLockComponent.lock("kobe"));
        assertFalse(redisLockComponent.lock("kobe"));
        assertTrue(redisLockComponent.unlock("kobe"));
        assertTrue(redisLockComponent.lock("kobe"));
        assertTrue(redisLockComponent.unlock("kobe"));
    }
}