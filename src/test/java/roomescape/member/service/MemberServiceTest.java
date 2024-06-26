package roomescape.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.member.dto.MemberDto;
import roomescape.member.dto.SaveMemberRequest;
import roomescape.member.model.MemberRole;

@SpringBootTest
@Sql(value = "classpath:test-data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @DisplayName("모든 회원 정보를 조회한다.")
    @Test
    void getMembersTest() {
        // When
        final List<MemberDto> members = memberService.getMembers();

        // Then
        assertThat(members).hasSize(5);
    }

    @DisplayName("회원 정보를 저장한다.")
    @Test
    void saveMemberTest() {
        // Given
        final MemberRole role = MemberRole.USER;
        final String password = "kellyPw1234";
        final String email = "kelly6bf@gmail.com";
        final String name = "kelly";
        final SaveMemberRequest saveMemberRequest = new SaveMemberRequest(email, password, name, role);

        // When
        final MemberDto savedMember = memberService.saveMember(saveMemberRequest);

        // Then
        assertAll(
                () -> assertThat(savedMember.id()).isEqualTo(6L),
                () -> assertThat(savedMember.email().getValue()).isEqualTo(email),
                () -> assertThat(savedMember.role()).isEqualTo(role),
                () -> assertThat(savedMember.name().getValue()).isEqualTo(name)
        );
    }

    @DisplayName("유효하지 않은 형식의 비밀번호가 입력되면 예외를 발생시킨다.")
    @NullAndEmptySource
    @ParameterizedTest
    void validatePlainPasswordFormatTest(final String password) {
        // Given
        final MemberRole role = MemberRole.USER;
        final String email = "kelly6bf@gmail.com";
        final String name = "kelly";
        final SaveMemberRequest saveMemberRequest = new SaveMemberRequest(email, password, name, role);

        // When & Then
        assertThatThrownBy(() -> memberService.saveMember(saveMemberRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 비밀번호로 공백을 입력할 수 없습니다.");
    }

    @DisplayName("유효하지 않은 길이의 비밀번호가 입력되면 예외를 발생시킨다.")
    @ValueSource(strings = {"aabbccdde", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"})
    @ParameterizedTest
    void validatePlainPasswordLengthTest(final String password) {
        // Given
        final MemberRole role = MemberRole.USER;
        final String email = "kelly6bf@gmail.com";
        final String name = "kelly";
        final SaveMemberRequest saveMemberRequest = new SaveMemberRequest(email, password, name, role);

        // When & Then
        assertThatThrownBy(() -> memberService.saveMember(saveMemberRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원 비밀번호 길이는 10이상 30이하여만 합니다.");
    }

    @DisplayName("이미 존재하는 이메일로 회원가입 요청이 들어오면 예외를 발생시킨다.")
    @Test
    void duplicateEmailTest() {
        // Given
        final String email = "user@mail.com";
        final String password = "userPw1234!";
        final MemberRole role = MemberRole.USER;
        final String name = "kelly";
        final SaveMemberRequest saveMemberRequest = new SaveMemberRequest(email, password, name, role);

        // When & Then
        assertThatThrownBy(() -> memberService.saveMember(saveMemberRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 존재하는 이메일입니다.");
    }
}
