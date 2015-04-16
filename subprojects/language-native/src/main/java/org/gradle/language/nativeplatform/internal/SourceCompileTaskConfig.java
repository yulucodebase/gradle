/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.language.nativeplatform.internal;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.util.PatternSet;
import org.gradle.language.base.LanguageSourceSet;
import org.gradle.language.base.internal.LanguageSourceSetInternal;
import org.gradle.language.base.internal.registry.LanguageTransform;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;
import org.gradle.language.nativeplatform.tasks.AbstractNativeSourceCompileTask;
import org.gradle.nativeplatform.ObjectFile;
import org.gradle.nativeplatform.internal.NativeBinarySpecInternal;

public class SourceCompileTaskConfig extends CompileTaskConfig {
    public SourceCompileTaskConfig(LanguageTransform<? extends LanguageSourceSet, ObjectFile> languageTransform, Class<? extends DefaultTask> taskType) {
        super(languageTransform, taskType);
    }

    protected void configureCompileTask(AbstractNativeCompileTask abstractTask, final NativeBinarySpecInternal binary, final LanguageSourceSetInternal sourceSet) {
        AbstractNativeSourceCompileTask task = (AbstractNativeSourceCompileTask) abstractTask;

        task.setDescription(String.format("Compiles the %s of %s", sourceSet, binary));

        task.source(sourceSet.getSource());

        final Project project = task.getProject();
        task.setObjectFileDir(project.file(String.valueOf(project.getBuildDir()) + "/objs/" + binary.getNamingScheme().getOutputDirectoryBase() + "/" + sourceSet.getFullName()));

        // If this task uses a pre-compiled header
        if (sourceSet instanceof DependentSourceSetInternal && ((DependentSourceSetInternal) sourceSet).getPreCompiledHeader() != null) {
            final DependentSourceSetInternal dependentSourceSet = (DependentSourceSetInternal)sourceSet;
            task.setPrefixHeaderFile(dependentSourceSet.getPrefixHeaderFile());
            task.setPreCompiledHeader(dependentSourceSet.getPreCompiledHeader());
            task.preCompiledHeaderInclude(binary.getPrefixFileToPCH().get(dependentSourceSet.getPrefixHeaderFile()));
        }

        binary.binaryInputs(task.getOutputs().getFiles().getAsFileTree().matching(new PatternSet().include("**/*.obj", "**/*.o")));
    }
}