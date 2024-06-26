package roomescape.member.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.dto.MemberDto;
import roomescape.member.dto.MemberResponse;
import roomescape.member.dto.SaveMemberRequest;
import roomescape.member.service.MemberService;

@RestController
public class MemberController {

    private final MemberService memberService;

    public MemberController(final MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/admin/members")
    public List<MemberResponse> getMembers() {
        return memberService.getMembers()
                .stream()
                .map(MemberResponse::from)
                .toList();
    }

    @PostMapping("/members")
    public MemberResponse saveMember(@RequestBody final SaveMemberRequest request) {
        final MemberDto savedMember = memberService.saveMember(request);
        return MemberResponse.from(savedMember);
    }
}
