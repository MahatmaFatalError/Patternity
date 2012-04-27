package com.patternity;

import java.io.File;
import java.util.Collection;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.patternity.annotation.DomainService;
import com.patternity.annotation.Entity;
import com.patternity.annotation.ValueObject;
import com.patternity.rule.basic.FinalFieldsRule;
import com.patternity.rule.basic.ForbiddenFieldDependencyRule;

/**
 * Goal to verify allowed dependencies.
 * 
 * Example: mvn patternity:verify-dependencies
 * 
 * @goal verify-dependencies
 * @phase test
 * 
 * @author Mohamed Bourogaa
 * @author Cyrille Martraire
 */
public class PatternityMojo extends AbstractMojo {

	/**
	 * Location of the file.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File outputDirectory;

	public void execute() throws MojoExecutionException, MojoFailureException {
		System.out.println("PatternityMojo verify-dependencies starting...");
		final Collection<Violation> violations = processClasses();
		if (!violations.isEmpty()) {
			printViolations(violations);
			throw new MojoFailureException(violations.toString());
		}
		System.out.println("PatternityMojo verify-dependencies done.");
	}

	private void printViolations(final Collection<Violation> violations) {
		System.err.println("PatternityMojo verify-dependencies found " + violations.size() + " violations: "
				+ violations);
		for (Violation violation : violations) {
			System.err.println(violation);
		}
	}

	protected Collection<Violation> processClasses() {
		System.out.println("PatternityMojo verify-dependencies starting...");
		final File root = new File(outputDirectory, "classes");

		final MetaModel metaModel = new MetaModelBuilder().build(root);
		System.out.println(metaModel);

		final RuleBook ruleBook = loadRuleBook();
		System.out.println(ruleBook);
		return new Processor(ruleBook).process(metaModel);
	}

	public RuleBook loadRuleBook() {
		final String vo = "com/patternity/annotation/ValueObject";
		final String entity = "com/patternity/annotation/Entity";
		final String service = "com/patternity/annotation/Service";
		final ForbiddenFieldDependencyRule vo2entity = new ForbiddenFieldDependencyRule(vo, entity);
		final ForbiddenFieldDependencyRule vo2service = new ForbiddenFieldDependencyRule(vo, service);
		final FinalFieldsRule voHazFinalFields = new FinalFieldsRule(vo);
		return new RuleBook(vo2entity, vo2service, voHazFinalFields);
	}

	@Override
	public String toString() {
		return "PatternityMojo 'verify-dependencies' outputDirectory=" + outputDirectory;
	}

}
