/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.dmn.versionconverter;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.drools.core.util.IoUtils;
import org.junit.Test;
import org.kie.api.builder.Message.Level;
import org.kie.api.io.Resource;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNMessage;
import org.kie.dmn.api.core.DMNModel;
import org.kie.dmn.api.core.DMNResult;
import org.kie.dmn.api.core.DMNRuntime;
import org.kie.dmn.core.internal.utils.DMNRuntimeBuilder;
import org.kie.dmn.model.api.Decision;
import org.kie.dmn.model.api.LiteralExpression;
import org.kie.dmn.validation.DMNValidator;
import org.kie.dmn.validation.DMNValidatorFactory;
import org.kie.internal.io.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.*;

public class ConversionTest {
    private static final Logger LOG = LoggerFactory.getLogger(ConversionTest.class);

    @Test
    public void test() throws Exception {
        File inputFile = new File(this.getClass().getResource("/model1.dmn").toURI());
        List<DMNMessage> modelValidation = validateModelFile(inputFile);
        LOG.info("Total of validation messages: {}", modelValidation.size());
        modelValidation.forEach(m -> LOG.info("{}", m));
        assertThat(modelValidation).noneMatch(m -> m.getLevel() == Level.ERROR);

        File convertedFile = Files.createTempFile("kie-dmn-versionconverter", ".dmn").toFile();
        KieDMNVersionConverterMain.process(inputFile, convertedFile);
        LOG.info("Output CONVERTED file written: {}", convertedFile.getName());
        LOG.debug("Converted file: \n{}", IoUtils.readFileAsString(convertedFile));

        List<DMNMessage> convertedValidation = validateModelFile(convertedFile);
        LOG.info("Total of CONVERTED validation messages: {}", convertedValidation.size());
        convertedValidation.forEach(m -> LOG.info("{}", m));
        assertThat(convertedValidation).noneMatch(m -> m.getLevel() == Level.ERROR);

        List<Resource> asList = Arrays.asList(ResourceFactory.newFileResource(convertedFile));
        DMNRuntime dmnRuntime = DMNRuntimeBuilder.fromDefaults().buildConfiguration().fromResources(asList)
                .getOrElseThrow(RuntimeException::new);
        DMNModel dmnModel = dmnRuntime.getModel("ns1", "model1");
        DMNContext dmnContext = dmnRuntime.newContext();
        dmnContext.set("a text", "asd");
        dmnContext.set("a number", 47);
        DMNResult evaluateAll = dmnRuntime.evaluateAll(dmnModel, dmnContext);
        LOG.info("{}", evaluateAll);
        assertThat(evaluateAll.hasErrors()).isFalse();
        assertThat(evaluateAll.getDecisionResultByName("d1").getResult()).asInstanceOf(InstanceOfAssertFactories.STRING)
                .isEqualTo("asd47");

        DMNResult evaluateDecisionService = dmnRuntime.evaluateDecisionService(dmnModel, dmnContext, "ds1");
        LOG.info("{}", evaluateDecisionService);
        assertThat(evaluateDecisionService.hasErrors()).isFalse();
        assertThat(evaluateDecisionService.getDecisionResultByName("d1").getResult())
                .asInstanceOf(InstanceOfAssertFactories.STRING).isEqualTo("asd47");
    }

    private List<DMNMessage> validateModelFile(File... inputFile) {
        return DMNValidatorFactory.newValidator()
                .validateUsing(DMNValidator.Validation.VALIDATE_SCHEMA, DMNValidator.Validation.VALIDATE_MODEL)
                .theseModels(inputFile);
    }

    @Test
    public void test_feelUnparsed() throws Exception {
        File inputFile = new File(this.getClass().getResource("/model2.dmn").toURI());
        List<DMNMessage> modelValidation = validateModelFile(inputFile);
        LOG.info("Total of validation messages: {}", modelValidation.size());
        modelValidation.forEach(m -> LOG.info("{}", m));
        assertThat(modelValidation).noneMatch(m -> m.getLevel() == Level.ERROR);

        File convertedFile = Files.createTempFile("kie-dmn-versionconverter", ".dmn").toFile();
        KieDMNVersionConverterMain.process(inputFile, convertedFile);
        LOG.info("Output CONVERTED file written: {}", convertedFile.getName());
        LOG.debug("Converted file: \n{}", IoUtils.readFileAsString(convertedFile));

        List<DMNMessage> convertedValidation = validateModelFile(convertedFile);
        LOG.info("Total of CONVERTED validation messages: {}", convertedValidation.size());
        convertedValidation.forEach(m -> LOG.info("{}", m));
        assertThat(convertedValidation).noneMatch(m -> m.getLevel() == Level.ERROR);

        List<Resource> asList = Arrays.asList(ResourceFactory.newFileResource(convertedFile));
        DMNRuntime dmnRuntime = DMNRuntimeBuilder.fromDefaults().buildConfiguration().fromResources(asList)
                .getOrElseThrow(RuntimeException::new);
        DMNModel dmnModel = dmnRuntime
                .getModel("http://www.trisotech.com/definitions/_8c871637-7aa0-4026-bb34-161ef3cff6ed", "Drawing 1");
        DMNContext dmnContext = dmnRuntime.newContext();
        DMNResult evaluateAll = dmnRuntime.evaluateAll(dmnModel, dmnContext);
        LOG.info("{}", evaluateAll);
        assertThat(evaluateAll.hasErrors()).isFalse();
        assertThat(evaluateAll.getDecisionResultByName("a decision").getResult())
                .asInstanceOf(InstanceOfAssertFactories.STRING).isEqualTo("Hello, World!");
        assertThat(((LiteralExpression) ((Decision) dmnModel.getDefinitions().getDrgElement().get(0)).getExpression())
                .getExpressionLanguage()).isNull();
    }

    @Test
    public void test_ImportName() throws Exception {
        File importedModelFile = new File(this.getClass().getResource("/Imported_Model.dmn").toURI());
        File inputFile = new File(this.getClass().getResource("/Importing_Model.dmn").toURI());

        File convertedFile = Files.createTempFile("kie-dmn-versionconverter", ".dmn").toFile();
        KieDMNVersionConverterMain.process(inputFile, convertedFile);
        LOG.info("Output CONVERTED file written: {}", convertedFile.getAbsolutePath());
        LOG.debug("Converted file: \n{}", IoUtils.readFileAsString(convertedFile));

        List<DMNMessage> convertedValidation = validateModelFile(importedModelFile, convertedFile);
        LOG.info("Total of CONVERTED validation messages: {}", convertedValidation.size());
        convertedValidation.forEach(m -> LOG.info("{}", m));
        assertThat(convertedValidation).noneMatch(m -> m.getLevel() == Level.ERROR);

        List<Resource> asList = Arrays.asList(ResourceFactory.newFileResource(importedModelFile), ResourceFactory.newFileResource(convertedFile));
        DMNRuntime dmnRuntime = DMNRuntimeBuilder.fromDefaults().buildConfiguration().fromResources(asList)
                .getOrElseThrow(RuntimeException::new);
        DMNModel dmnModel = dmnRuntime
                .getModel("http://www.trisotech.com/dmn/definitions/_f79aa7a4-f9a3-410a-ac95-bea496edab52", "Importing Model");
        DMNContext dmnContext = dmnRuntime.newContext();
        dmnContext.set("A Person", Map.of("age", new BigDecimal(47), 
                                               "name", "John"));
        DMNResult evaluateAll = dmnRuntime.evaluateAll(dmnModel, dmnContext);
        LOG.info("{}", evaluateAll);
        assertThat(evaluateAll.hasErrors()).isFalse();
        assertThat(evaluateAll.getDecisionResultByName("Greeting").getResult())
                .asInstanceOf(InstanceOfAssertFactories.STRING).isEqualTo("Hello John!");
    }

}