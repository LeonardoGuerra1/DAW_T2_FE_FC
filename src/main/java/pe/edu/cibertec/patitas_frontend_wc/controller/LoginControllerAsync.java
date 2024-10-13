package pe.edu.cibertec.patitas_frontend_wc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.cibertec.patitas_frontend_wc.client.AutenticationClient;
import pe.edu.cibertec.patitas_frontend_wc.dto.LoginRequestDTO;
import pe.edu.cibertec.patitas_frontend_wc.dto.LoginResponseDTO;
import pe.edu.cibertec.patitas_frontend_wc.dto.LogoutRequestDTO;
import pe.edu.cibertec.patitas_frontend_wc.dto.LogoutResponseDTO;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/app-login")
@CrossOrigin(origins = "http://localhost:5173")
public class LoginControllerAsync {

    @Autowired
    WebClient webClientAutenticacion;

    @Autowired
    AutenticationClient autenticationClient;

    @PostMapping("/autenticar-async")
    public Mono<LoginResponseDTO> autenticar(@RequestBody LoginRequestDTO loginRequestDTO) {
        // validar campos de entrada
        if (loginRequestDTO.tipoDocumento() == null || loginRequestDTO.tipoDocumento().trim().length() == 0 ||
                loginRequestDTO.numeroDocumento() == null || loginRequestDTO.numeroDocumento().trim().length() == 0 ||
                loginRequestDTO.password() == null || loginRequestDTO.password().trim().length() == 0){
            return Mono.just(new LoginResponseDTO("01", "Error: Debe completar correctamente sus credenciales", "", ""));
        }
        try {
            // consumir servicio backend de autenticacion
            return webClientAutenticacion.post()
                    .uri("/login")
                    .body(Mono.just(loginRequestDTO), LoginRequestDTO.class)
                    .retrieve()
                    .bodyToMono(LoginResponseDTO.class)
                    .flatMap(response -> {
                        if(response.codigo().equals("00")){
                            return Mono.just(new LoginResponseDTO("00", "", response.nombreUsuario(), ""));
                        } else {
                            return Mono.just(new LoginResponseDTO("02", "Error: Autenticación fallida", "", ""));
                        }
                    });
        } catch(Exception e) {
            System.out.println(e.getMessage());
            return Mono.just(new LoginResponseDTO("99", "Error: Ocurrió un problema en la autenticación", "", ""));
        }
    }

    @PostMapping("/salir")
    public LogoutResponseDTO logout(@RequestBody LogoutRequestDTO requestDTO) {
        try {
            ResponseEntity<LogoutResponseDTO> response = autenticationClient.logout(requestDTO);
            if (!response.getStatusCode().is2xxSuccessful())
                return new LogoutResponseDTO("02", null, "Error: No se pudo cerrar sesión.");

            LogoutResponseDTO responseDTO = response.getBody();
            if (responseDTO.codigo().equals("00"))
                return new LogoutResponseDTO("00", responseDTO.fecha(), "");
            else
                return new LogoutResponseDTO("01", null, "Error: No se pudo cerrar sesión.");
        } catch (Exception e) {
            return new LogoutResponseDTO("99", null, "Error: Algo salió mal.");
        }
    }
}
