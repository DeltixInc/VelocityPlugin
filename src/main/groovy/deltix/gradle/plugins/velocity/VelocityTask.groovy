package com.epam.deltix.gradle.plugins.velocity

import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

/**
 * Created by PastushenyaV on 7/26/2016.
 */
class VelocityTask extends DefaultTask {

    String from
    String to           //can be with parameters @{param_name}
    private Set<VelocityContext> velocityContexts = new HashSet<>()

    void setContexts(Collection<Map> contexts) {
        if (contexts == null || contexts.empty)
            return

        velocityContexts = new HashSet<>()

        for (Map context : contexts)
            velocityContexts.add(new VelocityContext(context))
    }

    @Input
    Set<Map> getContexts() {
        if (velocityContexts.isEmpty())
            return null

        Set<Map> contexts = new HashSet<>()
        for (VelocityContext velContext : velocityContexts) {
            Map contextMap = [:]
            for (String key in velContext.keys)
                contextMap.put(key, velContext.get(key))

            contexts.add(contextMap)
        }

        return contexts
    }

    @TaskAction
    doVelocity() {
        final VelocityEngine engine = new VelocityEngine()

        // disable velocity logs
        //engine.setProperty(VelocityEngine.RUNTIME_LOG_NAME, "org.apache.velocity.runtime.log.NullLogChute")

        FileReader reader
        FileWriter writer
        project.file(to).parentFile.mkdirs()

        for (VelocityContext context in velocityContexts) {
            String dest = new String(to)

            for (String key in context.getKeys()) {
                if (dest.indexOf('@{') == -1)
                    break;
                dest = dest.replace("@{$key}".toString(), (String) context.get(key))
            }

            writer = new FileWriter(project.file(dest))
            reader = new FileReader(project.file(from))

            engine.evaluate(context, writer, "Velocity", reader)

            writer.close();
            reader.close();
        }
    }
}
