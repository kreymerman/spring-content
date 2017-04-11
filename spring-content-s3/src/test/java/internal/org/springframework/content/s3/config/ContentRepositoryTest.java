package internal.org.springframework.content.s3.config;

import static com.github.paulcwarren.ginkgo4j.Ginkgo4jDSL.AfterEach;
import static com.github.paulcwarren.ginkgo4j.Ginkgo4jDSL.BeforeEach;
import static com.github.paulcwarren.ginkgo4j.Ginkgo4jDSL.Context;
import static com.github.paulcwarren.ginkgo4j.Ginkgo4jDSL.Describe;
import static com.github.paulcwarren.ginkgo4j.Ginkgo4jDSL.It;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.aws.core.io.s3.SimpleStorageResourceLoader;
import org.springframework.content.commons.annotations.Content;
import org.springframework.content.commons.annotations.ContentId;
import org.springframework.content.commons.repository.ContentStore;
import org.springframework.content.s3.config.AbstractS3ContentRepositoryConfiguration;
import org.springframework.content.s3.config.EnableS3ContentRepositories;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.github.paulcwarren.ginkgo4j.Ginkgo4jRunner;

import internal.org.springframework.content.commons.placement.UUIDPlacementStrategy;

@RunWith(Ginkgo4jRunner.class)
public class ContentRepositoryTest {

	private AnnotationConfigApplicationContext context;
	{
		Describe("EnableS3ContentRepositories", () -> {
			Context("given a context and a configuartion with an S3 content repository bean", () -> {
				BeforeEach(() -> {
					context = new AnnotationConfigApplicationContext();
					context.register(TestConfig.class);
					context.refresh();
				});
				AfterEach(() -> {
					context.close();
				});
				It("should have a Content Repository bean", () -> {
					assertThat(context.getBean(TestEntityContentRepository.class), is(not(nullValue())));
				});
				It("should have a default UUIDPlacementStrategy bean", () -> {
					assertThat(context.getBean(UUIDPlacementStrategy.class), is(not(nullValue())));
				});
			});
			Context("given a context with an empty configuration", () -> {
				BeforeEach(() -> {
					context = new AnnotationConfigApplicationContext();
					context.register(EmptyConfig.class);
					context.refresh();
				});
				AfterEach(() -> {
					context.close();
				});
				It("should not contains any S3 repository beans", () -> {
					try {
						context.getBean(TestEntityContentRepository.class);
						fail("expected no such bean");
					} catch (NoSuchBeanDefinitionException e) {
						assertThat(true, is(true));
					}
				});
			});
		});
	}


	@Test
	public void noop() {
	}

	@Configuration
	@EnableS3ContentRepositories(basePackages="contains.no.fs.repositores")
//	@EnableContextResourceLoader
	@Import(InfrastructureConfig.class)
	public static class EmptyConfig {
	}

	@Configuration
	@EnableS3ContentRepositories
//	@EnableContextResourceLoader
	@Import(InfrastructureConfig.class)
	public static class TestConfig {
	}

	@Configuration
	public static class InfrastructureConfig extends AbstractS3ContentRepositoryConfiguration {

		@Autowired
		private AmazonS3 client;
		
		public Region region() {
			return Region.getRegion(Regions.US_WEST_1);
		}
		
		@Override
		public SimpleStorageResourceLoader simpleStorageResourceLoader() {
	        client.setRegion(region());
			return new SimpleStorageResourceLoader(client);
		}
	}

	@Content
	public class TestEntity {
		@ContentId
		private String contentId;
	}

	public interface TestEntityContentRepository extends ContentStore<TestEntity, String> {
	}
}
