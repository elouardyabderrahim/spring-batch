package com.batch.customer.config;


import com.batch.customer.entity.Customer;
import com.batch.customer.repository.CustomerRepository;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@AllArgsConstructor
public class SpringBatchConfig {

    private CustomerRepository customerRepository;

       // Create the reader
    @Bean
    public FlatFileItemReader customerReader() {
        return new FlatFileItemReaderBuilder<>().name("coffeeItemReader")
                .resource(new ClassPathResource("customers.csv"))
                .linesToSkip(1)
                .delimited()
                .names(new String[] { "id", "firstName", "lastName", "email", "gender", "contactNo", "country", "dob" })
                .fieldSetMapper(new BeanWrapperFieldSetMapper() {{
                    setTargetType(Customer.class);
                }})
                .build();
    }
    //the commented code is the first code but i had a problem with the reader FlatFileItemReader changed to FlatFileItemReaderBuilder:
  /*  public FlatFileItemReader<Customer> customerReader() {

        FlatFileItemReader<Customer> itemReader = new FlatFileItemReader<>();
        Resource resource = new FileSystemResource("customers.csv");
        itemReader.setResource(resource);
        itemReader.setName("csv_customer_reader");
        itemReader.setLinesToSkip(1);
        itemReader.setLineMapper(lineMapper());


        return itemReader;
    }

   */

    /*private LineMapper<Customer> lineMapper() {

        DefaultLineMapper<Customer> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);// allow some empty columns
        lineTokenizer.setNames("id", "firstName", "lastName", "email", "gender", "contactNo", "country", "dob");

        // convert data to customer bean object
        BeanWrapperFieldSetMapper<Customer> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Customer.class);

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        return lineMapper;
    }

     */


    // create processor:
    @Bean
    public CustomerProcessor customerProcessor() {
        return new CustomerProcessor();
    }

    // create writer:
    @Bean
    public RepositoryItemWriter<Customer> customerWriter(){
        RepositoryItemWriter<Customer> customerRepositoryItemWriter=new RepositoryItemWriter<>();
        customerRepositoryItemWriter.setRepository(customerRepository);
        customerRepositoryItemWriter.setMethodName("save");
        return customerRepositoryItemWriter;
    }

// create Step
    @Bean
    public Step step(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){
        return new StepBuilder("stepOne",jobRepository).<Customer,Customer> chunk(10,platformTransactionManager)
                .reader(customerReader())
                .processor(customerProcessor())
                .writer(customerWriter())
                .build();
    }

// create Job
@Bean
public Job sampleJob(JobRepository jobRepository,Step step) {
    return new JobBuilder("jobOne", jobRepository)
            .flow(step)
            .end()
            .build();
}

}
