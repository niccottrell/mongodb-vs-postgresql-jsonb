package niccottrell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

// @EnableMongoRepositories
// @EnableJpaRepositories
// @EnableAutoConfiguration
public class App {

  private static final Logger logger = LoggerFactory.getLogger(App.class);

  public App() {
    logger.info("Creating App");
  }

  public static void main(String[] args) throws Exception {
    logger.info("App.main");
    AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
    ctx.register(Populate.class);
    ctx.refresh();

    Populate populate = BeanFactoryUtils.beanOfType(ctx, Populate.class);
    populate.createIndexes();
    populate.go();
  }

}
