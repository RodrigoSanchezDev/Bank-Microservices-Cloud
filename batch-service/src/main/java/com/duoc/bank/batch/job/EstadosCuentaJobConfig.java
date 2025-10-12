package com.duoc.bank.batch.job;

import com.duoc.bank.batch.model.CuentaAnualCSV;
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
 * Configuración del Job de Generación de Estados de Cuenta Anuales
 * Lee archivo CSV de cuentas anuales y genera reportes de auditoría
 */
@Configuration
public class EstadosCuentaJobConfig {

    @Bean
    public Job estadosCuentaAnualesJob(JobRepository jobRepository, Step processEstadosCuentaStep) {
        return new JobBuilder("estadosCuentaAnualesJob", jobRepository)
                .start(processEstadosCuentaStep)
                .build();
    }

    @Bean
    public Step processEstadosCuentaStep(JobRepository jobRepository,
                                         PlatformTransactionManager transactionManager,
                                         FlatFileItemReader<CuentaAnualCSV> estadoCuentaReader,
                                         EstadoCuentaProcessor estadoCuentaProcessor,
                                         EstadoCuentaWriter estadoCuentaWriter) {
        return new StepBuilder("processEstadosCuentaStep", jobRepository)
                .<CuentaAnualCSV, CuentaAnualCSV>chunk(10, transactionManager)
                .reader(estadoCuentaReader)
                .processor(estadoCuentaProcessor)
                .writer(estadoCuentaWriter)
                .faultTolerant()
                .retry(Exception.class)
                .retryLimit(3)
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<CuentaAnualCSV> estadoCuentaReader() {
        return new FlatFileItemReaderBuilder<CuentaAnualCSV>()
                .name("estadoCuentaReader")
                .resource(new ClassPathResource("data/semana_3/cuentas_anuales.csv"))
                .delimited()
                .delimiter(",")
                .names("id", "cuentaId", "titular", "tipoCuenta", "saldoInicial", 
                       "saldoFinal", "totalTransacciones", "periodo", "estado")
                .linesToSkip(1) // Saltar encabezado
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(CuentaAnualCSV.class);
                }})
                .build();
    }
}
