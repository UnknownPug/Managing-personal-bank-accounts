package accounts.bank.managing.thesis.bachelor.rastvdmy.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@ToString
@Entity
@Table(name = "user_profile")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Enumerated(EnumType.STRING)
    private UserVisibility visibility;

    @Column(name = "name", nullable = false)
    @Size(min = 2, max = 10, message = "Name should be between 2 and 10 characters")
    private String name;

    @Column(name = "surname", nullable = false)
    @Size(min = 2, max = 15, message = "Surname should be between 2 and 15 characters")
    private String surname;

    @Column(name = "date_of_birth", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd", iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @Column(name = "country_of_origin", nullable = false)
    private String countryOrigin;

    @Column(name = "email", nullable = false)
    @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}",
            flags = Pattern.Flag.CASE_INSENSITIVE, message = "Email must contain valid tags.")
    private String email;

    @Column(name = "password", nullable = false)
    @Size(min = 8, max = 20, message = "Password should be between 8 and 20 characters")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9\\W]).{8,20}$",
            message = "Password must contain at least one uppercase letter and one number or symbol")
    private String password;

    @Column(name = "avatar", nullable = false)
    private String avatar;

    @Column(name = "phone_number", nullable = false)
    @Pattern(regexp = "^(\\+\\d{1,3})?\\d{9,15}$", message = "Phone number should be in international format")
    private String phoneNumber;

    @JsonIgnore
    @ManyToMany
    @ToString.Exclude
    private List<CurrencyData> currencyData;

    @JsonIgnore
    @OneToOne(mappedBy = "userLoan", cascade = CascadeType.ALL)
    @ToString.Exclude
    private BankLoan bankLoan;

    @JsonIgnore
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Message> senderMessages;

    @JsonIgnore
    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Message> receiverMessages;


    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Card> cards;

    // Default Constructor for user
    public User() {
        this.userRole = UserRole.ROLE_USER;
        this.visibility = UserVisibility.STATUS_ONLINE;
        this.status = UserStatus.STATUS_DEFAULT;
        this.avatar = "https://i0.wp.com/sbcf.fr/wp-content/uploads/2018/03/sbcf-default-avatar.png?ssl=1";
        this.cards = new ArrayList<>();
    }

    public User(UserRole role) {
        this.userRole = role;
        this.visibility = UserVisibility.STATUS_ONLINE;
    }

    public void encodePassword(PasswordEncoder encoder) {
        this.password = encoder.encode(password);
    }

    public void erasePassword() {
        this.password = null;
    }
}
