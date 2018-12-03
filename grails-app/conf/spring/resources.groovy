import com.example.AutoUserstampEventListener
import com.example.SnowflakeIdGenerator

// Place your Spring DSL code here
beans = {
    idGenerator(SnowflakeIdGenerator)

    autoUserstampEventListener(AutoUserstampEventListener, ref('neo4jDatastore')) {
        securityService = ref('securityService')
    }
}
