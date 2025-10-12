package com.duoc.bank.batch.job;

import com.duoc.bank.batch.model.TransaccionCSV;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TransaccionesDiariasJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job transaccionesDiariasJob() {
        return new JobBuilder("transaccionesDiariasJob", jobRepository)
                .start(processTransaccionesStep())
                .build();
    }

    @Bean
    public Step processTransaccionesStep() {
        return new StepBuilder("processTransaccionesStep", jobRepository)
                .<TransaccionCSV, TransaccionCSV>chunk(10, transactionManager)
                .reader(transaccionReader())
                .processor(transaccionProcessor())
                .writer(transaccionWriter())
                .faultTolerant()
                .retry(Exception.class)
                .retryLimit(3)
                .build();
    }

    @Bean
    public FlatFileItemReader<TransaccionCSV> transaccionReader() {
        FlatFileItemReader<TransaccionCSV> reader = new FlatFileItemReader<>();
        reader.setResource(new ClassPathResource("data/semana_1/transacciones.csv"));
        reader.setLinesToSkip(1); // Saltar header
        reader.setLineMapper(transaccionLineMapper());
        return reader;
    }

    private DefaultLineMapper<TransaccionCSV> transaccionLineMapper() {
        DefaultLineMapper<TransaccionCSV> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("id", "tipo", "monto", "fecha", "cuentaId", "descripcion", "estado");
        tokenizer.setDelimiter(",");

        BeanWrapperFieldSetMapper<TransaccionCSV> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(TransaccionCSV.class);

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;
    }

    @Bean
    public TransaccionProcessor transaccionProcessor() {
        return new TransaccionProcessor();
    }

    @Bean
    public TransaccionWriter transaccionWriter() {
        return new TransaccionWriter();
    }
}
