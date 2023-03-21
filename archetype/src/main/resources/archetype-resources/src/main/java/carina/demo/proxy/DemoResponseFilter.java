#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.carina.demo.proxy;

import com.browserup.bup.filters.ResponseFilter;
import com.browserup.bup.util.HttpMessageContents;
import com.browserup.bup.util.HttpMessageInfo;

import io.netty.handler.codec.http.HttpResponse;

public class DemoResponseFilter implements ResponseFilter {

    private final String textToReplace;
    private final String replacementText;

    /**
     * @param textToReplace   text that should be replaced in response
     * @param replacementText replacement text
     */
    public DemoResponseFilter(String textToReplace, String replacementText) {
        this.textToReplace = textToReplace;
        this.replacementText = replacementText;
    }

    @Override
    public void filterResponse(HttpResponse response, HttpMessageContents contents, HttpMessageInfo messageInfo) {
        if (contents.getTextContents().contains(textToReplace)) {
            contents.setTextContents(contents.getTextContents()
                    .replace(textToReplace, replacementText));
        }
    }

}
