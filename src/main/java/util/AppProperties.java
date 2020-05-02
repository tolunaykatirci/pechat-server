package util;


import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AppProperties {

    private int port;
    private String databasePath;
    private String publicKeyPath;
    private String privateKeyPath;
    private String clientCertificateFolder;
}
