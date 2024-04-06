package accounts.bank.managing.thesis.bachelor.rastvdmy.service;

import accounts.bank.managing.thesis.bachelor.rastvdmy.entity.Message;
import accounts.bank.managing.thesis.bachelor.rastvdmy.exception.ApplicationException;
import accounts.bank.managing.thesis.bachelor.rastvdmy.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {
    private final MessageRepository messageRepository;

    @Autowired
    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Cacheable(value = "messages")
    public List<Message> getMessages() {
        return messageRepository.findAll();
    }

    @Cacheable(value = "messages", key = "#messageId")
    public Message getMessageById(Long messageId) {
        return messageRepository.findById(messageId).orElseThrow(
                () -> new ApplicationException(HttpStatus.NOT_FOUND, "Message with id " + messageId + " not found.")
        );
    }

    @Cacheable(value = "messages", key = "#content")
    public List<Message> getMessagesByContent(String content) {
        if (content.isEmpty()) {
            throw new ApplicationException(HttpStatus.NOT_FOUND, "Message " + content + " not found.");
        }
        return messageRepository.findByContent(content);
    }

    @Cacheable(value = "messages", key = "#senderId")
    public List<Message> getSortedMessagesBySenderId(Long senderId) {
        List<Message> messages = messageRepository.findAll();
        return messages.stream()
                .sorted()
                .filter(message -> message.getSender().getId().equals(senderId))
                .toList();
    }

    @Cacheable(value = "messages", key = "#receiverId")
    public List<Message> getSortedMessagesByReceiverId(Long receiverId) {
        List<Message> messages = messageRepository.findAll();
        return messages.stream()
                .sorted()
                .filter(message -> message.getReceiver().getId().equals(receiverId))
                .toList();
    }

    @CachePut(value = "messages", key = "#result.id")
    public Message sendMessage(Long senderId, Long receiverId, String content) {
        // TODO: complete this method
        return null;
    }
}
