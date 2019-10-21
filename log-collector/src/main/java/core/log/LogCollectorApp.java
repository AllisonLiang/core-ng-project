package core.log;

import core.framework.http.HTTPMethod;
import core.framework.log.message.EventMessage;
import core.framework.log.message.LogTopics;
import core.framework.module.App;
import core.framework.module.SystemModule;
import core.framework.util.Sets;
import core.framework.util.Strings;
import core.log.web.EventController;
import core.log.web.SendEventRequest;
import core.log.web.SendEventRequestValidator;

import java.util.Set;

/**
 * @author neo
 */
public class LogCollectorApp extends App {
    @Override
    protected void initialize() {
        load(new SystemModule("sys.properties"));
        loadProperties("app.properties");
        http().maxForwardedIPs(3);      // loose x-forwarded-for ip config, there are cdn/proxy before system, and in event collector, preventing fake client ip is less important

        site().security();
        site().staticContent("/robots.txt");

        kafka().publish(LogTopics.TOPIC_EVENT, EventMessage.class);

        bind(SendEventRequestValidator.class);

        Set<String> allowedOrigins = allowedOrigins(requiredProperty("app.allowedOrigins"));
        EventController controller = bind(new EventController(allowedOrigins));
        http().route(HTTPMethod.OPTIONS, "/event/:app", controller::options);
        http().route(HTTPMethod.POST, "/event/:app", controller::post);  // event will be sent via ajax or navigator.sendBeacon(), refer to https://developer.mozilla.org/en-US/docs/Web/API/Navigator/sendBeacon
        http().bean(SendEventRequest.class);
    }

    Set<String> allowedOrigins(String value) {
        String[] origins = Strings.split(value, ',');
        Set<String> result = Sets.newHashSetWithExpectedSize(origins.length);
        for (String origin : origins) {
            result.add(origin.strip());
        }
        return result;
    }
}
