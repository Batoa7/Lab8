package mn.edu.must.sqat;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseRegistrationServiceTest {

    @Mock StudentRepository repository;
    @Mock EmailSender emailSender;

    CourseRegistrationService service;

    // Туслах өгөгдөл: CS101-ийг үзсэн оюутан
    static final String EMAIL = "s1@must.edu.mn";
    final Student activeStudent = new Student("S1", EMAIL, true, List.of("CS101"));
    final Student inactiveStudent = new Student("S1", EMAIL, false, List.of("CS101"));

    @BeforeEach
    void setUp() {
        service = new CourseRegistrationService(repository, emailSender);
    }

    // A1: stub (when) + verify
    @Test
    void register_activeStudent_returnsOkSavesAndSendsEmail() {
        when(repository.findStudent("S1")).thenReturn(activeStudent);
        when(repository.courseExists("CS201")).thenReturn(true);
        when(repository.prerequisites("CS201")).thenReturn(List.of("CS101"));

        String result = service.register("S1", "CS201");

        assertEquals("OK", result);
        verify(repository).saveRegistration("S1", "CS201");
        verify(emailSender).send(eq(EMAIL), anyString(), anyString());
    }

    // A2: оюутан байхгүй -> never()
    @Test
    void register_unknownStudent_returnsNoStudentAndDoesNotSaveOrEmail() {
        when(repository.findStudent("S1")).thenReturn(null);

        String result = service.register("S1", "CS201");

        assertEquals("ERROR_NO_STUDENT", result);
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verify(emailSender, never()).send(anyString(), anyString(), anyString());
    }

    // A3: идэвхгүй оюутан -> verifyNoInteractions
    @Test
    void register_inactiveStudent_returnsInactiveAndNeverTouchesEmail() {
        when(repository.findStudent("S1")).thenReturn(inactiveStudent);

        String result = service.register("S1", "CS201");

        assertEquals("ERROR_INACTIVE_STUDENT", result);
        verifyNoInteractions(emailSender);
        verify(repository, never()).saveRegistration(anyString(), anyString());
    }

    // A4: өмнөх холбоо хичээл дутуу (CS102 үзээгүй)
    @Test
    void register_missingPrerequisite_returnsPrerequisitesErrorAndDoesNotSave() {
        when(repository.findStudent("S1")).thenReturn(activeStudent);
        when(repository.courseExists("CS201")).thenReturn(true);
        when(repository.prerequisites("CS201")).thenReturn(List.of("CS101", "CS102"));

        String result = service.register("S1", "CS201");

        assertEquals("ERROR_PREREQUISITES", result);
        verify(repository, never()).saveRegistration(anyString(), anyString());
    }

    // A5: doThrow — и-мэйл унасан ч бүртгэл хүчинтэй
    @Test
    void register_emailSenderThrows_stillReturnsOkAndSaves() {
        when(repository.findStudent("S1")).thenReturn(activeStudent);
        when(repository.courseExists("CS201")).thenReturn(true);
        when(repository.prerequisites("CS201")).thenReturn(List.of("CS101"));
        doThrow(new RuntimeException("SMTP down"))
                .when(emailSender).send(anyString(), anyString(), anyString());

        String result = service.register("S1", "CS201");

        assertEquals("OK", result);
        verify(repository).saveRegistration("S1", "CS201");
    }

    // A6: null оролт — хоёр аргумент тус бүрийг шалгана (Хэсэг B мутант 3-ыг барина)
    @Test
    void register_nullArgument_throwsIllegalArgumentAndNeverTouchesDependencies() {
        assertThrows(IllegalArgumentException.class, () -> service.register(null, "CS201"));
        assertThrows(IllegalArgumentException.class, () -> service.register("S1", null));

        verifyNoInteractions(repository, emailSender);
    }

    // A7: ArgumentCaptor — и-мэйлийн биед хичээлийн код байна
    @Test
    void register_success_emailBodyContainsCourseId() {
        when(repository.findStudent("S1")).thenReturn(activeStudent);
        when(repository.courseExists("CS201")).thenReturn(true);
        when(repository.prerequisites("CS201")).thenReturn(List.of("CS101"));
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        service.register("S1", "CS201");

        verify(emailSender).send(eq(EMAIL), anyString(), bodyCaptor.capture());
        assertTrue(bodyCaptor.getValue().contains("CS201"));
    }

    // A8: гурван алдааны анги нэг тестээр.
    // Мөр бүр өөр stub ашигладаг тул lenient() хэрэгтэй (strict stubs алдаа өгөхөөс сэргийлнэ).
    @ParameterizedTest
    @CsvSource({
            "false, false, true,  ERROR_NO_STUDENT",
            "true,  false, true,  ERROR_INACTIVE_STUDENT",
            "true,  true,  false, ERROR_PREREQUISITES"
    })
    void register_invalidScenario_returnsExpectedErrorAndDoesNotSave(
            boolean studentFound, boolean active, boolean hasAllPrereqs, String expected) {
        Student student = new Student("S1", EMAIL, active, List.of("CS101"));
        lenient().when(repository.findStudent("S1")).thenReturn(studentFound ? student : null);
        lenient().when(repository.courseExists("CS201")).thenReturn(true);
        lenient().when(repository.prerequisites("CS201"))
                .thenReturn(hasAllPrereqs ? List.of("CS101") : List.of("CS101", "CS999"));

        String result = service.register("S1", "CS201");

        assertEquals(expected, result);
        verify(repository, never()).saveRegistration(anyString(), anyString());
        verify(emailSender, never()).send(anyString(), anyString(), anyString());
    }
}
