package com.cleartax.training_superheroes.services;

import com.cleartax.training_superheroes.dto.Superhero;
import com.cleartax.training_superheroes.dto.SuperheroRequestBody;
import com.cleartax.training_superheroes.repos.SuperheroRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;

@Service
public class ConsumerService {

    @Autowired
    private SuperheroService superheroService;

    @Autowired
    private SuperheroRepository superheroRepository;

    @Autowired
    private SqsClient sqsClient;

    @Value("${sqs.queue.url}")
    private String queueUrl;

    // Poll the queue every 5 seconds
    @Scheduled(fixedRate = 5000)
    public void pollQueue() {
        try {
            // Receive messages from the queue
            ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(1)  // Retrieve up to 10 messages at once
                    .waitTimeSeconds(20) // Long polling to wait for up to 20 seconds
                    .build();

            ReceiveMessageResponse response = sqsClient.receiveMessage(receiveMessageRequest);
            List<Message> messages = response.messages();

            if (messages.isEmpty()) {
                System.out.println("No messages in the queue.");
            } else {
                // Process each message
                for (Message message : messages) {
                    System.out.println("Processing message with ID: " + message.messageId());
                    System.out.println("Message Body: " + message.body());

                    // Delete the message from the queue after processing
                    deleteMessage(message);
                }
            }
        } catch (SqsException e) {
            System.err.println("Error receiving messages: " + e.awsErrorDetails().errorMessage());
        }
    }

    private void deleteMessage(Message message) {
        try {
            DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(message.receiptHandle())
                    .build();
            sqsClient.deleteMessage(deleteMessageRequest);
            String superHeroName = message.body();
            Superhero superhero = superheroRepository.findByName(superHeroName)
                    .orElseThrow(() -> new RuntimeException("Superhero not found with name: " + superHeroName));
            superheroService.updateSuperhero(superHeroName,
                    new SuperheroRequestBody(superHeroName,
                            "Updated Power = " + superhero.getPower()
                            , "Updated Universe = " + superhero.getUniverse()));


            System.out.println("Message with ID " + message.messageId() + message.body() + " has been deleted from the queue.");
        } catch (SqsException e) {
            System.err.println("Error deleting message: " + e.awsErrorDetails().errorMessage());
        }
    }
}
