package core.framework.impl.template.fragment;

import core.framework.impl.template.CDNManager;
import core.framework.impl.template.TemplateContext;
import core.framework.impl.template.TemplateMetaContext;
import core.framework.impl.template.expression.ExpressionBuilder;
import core.framework.impl.template.expression.ExpressionHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author neo
 */
public class CDNFragment implements Fragment {
    private final Logger logger = LoggerFactory.getLogger(CDNFragment.class);

    private final ExpressionHolder expression;
    private final String location;

    public CDNFragment(String expression, TemplateMetaContext context, String location) {
        this.expression = new ExpressionBuilder(expression, context, location).build();
        this.location = location;
    }

    @Override
    public void process(StringBuilder builder, TemplateContext context) {
        String url = String.valueOf(expression.eval(context));
        builder.append(cdn(url, context.cdn));
    }

    private String cdn(String url, CDNManager cdn) {
        //TODO: better way to prevent XSS?
        if (url.contains("javascript:") || url.contains("<") || url.contains(">") || url.contains(" ")) {
            logger.warn("illegal url detected, url={}, location={}", url, location);
            return "";
        }
        return cdn.url(url);
    }
}