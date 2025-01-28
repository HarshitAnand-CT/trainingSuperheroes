package com.cleartax.training_superheroes.controllers;


import com.cleartax.training_superheroes.config.SqsClientConfig;
import com.cleartax.training_superheroes.config.SqsConfig;
import com.cleartax.training_superheroes.dto.Superhero;
import com.cleartax.training_superheroes.dto.SuperheroRequestBody;
import com.cleartax.training_superheroes.services.SuperheroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.List;

@RestController
public class SuperheroController {

    private SuperheroService superheroService;

    @Autowired
    private SqsConfig sqsConfig;

    @Autowired
    private SqsClient sqsClient;

    @Autowired
    public SuperheroController(SuperheroService superheroService){
        this.superheroService = superheroService;
    }

    @GetMapping("/hello")
    public String hello(@RequestParam(value = "username", defaultValue = "World") String username, @RequestBody String heroName) {
        sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(sqsConfig.getQueueUrl())
                .messageBody(heroName).build());
        return String.format("Hello %s ,%s!", username, sqsConfig.getQueueName());
    }

    @GetMapping("/superhero")
    public Superhero getSuperhero(@RequestParam(value = "name", defaultValue = "Batman") String name,
                                  @RequestParam(value = "universe", defaultValue = "DC") String universe){
        return superheroService.getSuperhero(name, universe);
    }

    @PostMapping("/superhero")
    public Superhero persistSuperhero(@RequestBody SuperheroRequestBody superhero){
        return superheroService.persistSuperhero(superhero);
    }

    @RequestMapping(value = "/superhero/{heroName}", method = RequestMethod.PUT)
    public Superhero updateSuperhero(@PathVariable("heroName") String heroName, @RequestBody SuperheroRequestBody updateEntry) {
        System.out.println("Received heroName: " + heroName);
        return superheroService.updateSuperhero(heroName, updateEntry);
    }

    @RequestMapping(value = "/superhero/{heroName}", method = RequestMethod.DELETE)
    public void deleteSuperhero(@PathVariable("heroName") String heroName) {
        System.out.println("Received heroName: " + heroName);
        superheroService.deleteSuperhero(heroName);
    }

}
