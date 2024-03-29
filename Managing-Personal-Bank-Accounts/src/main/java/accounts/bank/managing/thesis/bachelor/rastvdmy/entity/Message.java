package accounts.bank.managing.thesis.bachelor.rastvdmy.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@Table(name = "message")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "content", nullable = false)
    @Size(min = 1, max = 100, message = "The length of the message must be between 1 and 100 characters")
    private String content;

    @Column(name = "timestamp", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm", iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime timestamp;

    @ManyToOne
    @ToString.Exclude
    @Size(min = 1, max = 100, message = "The length of the message must be between 1 and 100 characters")
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @ToString.Exclude
    @Size(min = 1, max = 100, message = "The length of the message must be between 1 and 100 characters")
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;
}
