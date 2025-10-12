package com.duoc.bank.batch.job;

import com.duoc.bank.batch.model.InteresCSV;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Configuración del Job de Cálculo de Intereses Mensuales
 * Lee archivo CSV de intereses y aplica cálculos a las cuentas
 */
@Configuration
public class InteresesJobConfig {

    @Bean
    public Job interesesMensualesJob(JobRepository jobRepository, Step processInteresesStep) {
        return new JobBuilder("interesesMensualesJob", jobRepository)
                .start(processInteresesStep)
                .build();
    }

    @Bean
    public Step processInteresesStep(JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager,
                                     FlatFileItemReader<InteresCSV> interesReader,
                                     InteresProcessor interesProcessor,
                                     InteresWriter interesWriter) {
        return new StepBuilder("processInteresesStep", jobRepository)
                .<InteresCSV, InteresCSV>chunk(10, transactionManager)
                .reader(interesReader)
                .processor(interesProcessor)
                .writer(interesWriter)
                .faultTolerant()
                .retry(Exception.class)
                .retryLimit(3)
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<InteresCSV> interesReader() {
        return new FlatFileItemReaderBuilder<InteresCSV>()
                .name("interesReader")
                .resource(new ClassPathResource("data/semana_2/intereses.csv"))
                .delimited()
                .delimiter(",")
                .names("id", "cuentaId", "tasaInteres", "montoInteres", "periodo", "estado")
                .linesToSkip(1) // Saltar encabezado
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(InteresCSV.class);
                }})
                .build();
    }
}
