package com.puplopez.clms.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.puplopez.clms.model.Computer;
import com.puplopez.clms.model.Laboratory;
import com.puplopez.clms.repository.ComputerRepository;
import com.puplopez.clms.repository.LaboratoryRepository;

@Configuration
public class LabDataSeeder {

    @Bean
    public CommandLineRunner seedLabsAndComputers(LaboratoryRepository labRepo, ComputerRepository computerRepo) {
        return args -> {
            if (labRepo.count() == 0) {
                for (String name : List.of("Laboratory 1", "Laboratory 2", "Laboratory 3")) {
                    Laboratory lab = new Laboratory();
                    lab.setLabName(name);
                    lab = labRepo.save(lab);

                    for (int i = 1; i <= 5; i++) {
                        Computer pc = new Computer();
                        pc.setLaboratory(lab);
                        pc.setComputerNumber(String.format("PC-%02d", i));
                        pc.setStatus("Working");
                        computerRepo.save(pc);
                    }
                }
            }
        };
    }
}