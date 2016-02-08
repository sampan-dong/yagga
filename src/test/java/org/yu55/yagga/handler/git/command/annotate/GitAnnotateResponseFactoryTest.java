package org.yu55.yagga.handler.git.command.annotate;

import static org.yu55.yagga.handler.git.command.annotate.GitAnnotateResponseFactory.factorizeAnnotateResponse;

import org.junit.Test;
import org.yu55.yagga.core.annotate.model.AnnotateResponse;
import org.yu55.yagga.core.annotate.model.AnnotateResponseLineAssert;
import org.yu55.yagga.handler.generic.command.CommandOutput;
import org.yu55.yagga.handler.generic.command.CommandOutputLine;

public class GitAnnotateResponseFactoryTest {

    @Test
    public void testFactorizeAnnotateResponse() throws Exception {
        // given
        CommandOutput annotateCommandOutput = new CommandOutput();
        annotateCommandOutput.addOutputLine(new CommandOutputLine(
                "af7545c8\t( Mikołaj    2015-11-21 20:07:59 +0100    1)package org.yu55.yagga.handler.api.command.grep;"
        ));

        // when
        AnnotateResponse annotateResponse = factorizeAnnotateResponse(annotateCommandOutput);

        // then
        AnnotateResponseLineAssert.assertThat(annotateResponse.getAnnotationResponseLines().get(0))
                .hasCommitId("af7545c8")
                .hasAuthor("Mikołaj")
                .hasCommitDate("2015-11-21 20:07:59 +0100")
                .hasLineNumber(1)
                .hasLine("package org.yu55.yagga.handler.api.command.grep;");
    }
}