# Лаб 8: Mockito mock объектууд

**Оюутан:** Баттөмөр, b242270025

[![Tests](https://github.com/Batoa7/Lab8/actions/workflows/test.yml/badge.svg)](https://github.com/Batoa7/Lab8/actions)

## Орчин

```
openjdk version "21.0.13" 2026-10-20
OpenJDK Runtime Environment (build 21.0.13+9-1-deb13u2-Debian)
OpenJDK 64-Bit Server VM (build 21.0.13+9-1-deb13u2-Debian, mixed mode, sharing)
[1mApache Maven 3.9.9[m
Maven home: /usr/share/maven
Java version: 21.0.13, vendor: Debian, runtime: /usr/lib/jvm/java-21-openjdk-amd64
Default locale: en_US, platform encoding: UTF-8
OS name: "linux", version: "6.12.107+deb13-amd64", arch: "amd64", family: "unix"
```

## Тестийн тоо

`results/mvn-test.txt`-ийн сүүлийн мөр: **Tests run: 10**, Failures: 0, Errors: 0, Skipped: 0 (7 энгийн тест + A8-ийн 3 мөр).

## A8-ийн lenient()

A8-д мөр бүр өөр stub ашигладаг: жишээ нь "оюутан байхгүй" мөрөнд `courseExists`, `prerequisites` stub хэрэглэгдэхгүй. Strict stubs горим ашиглагдаагүй stub дээр UnnecessaryStubbingException өгдөг тул `lenient().when(...)` хэрэглэсэн.

## Хэсэг B: мутацийн хүснэгт

| № | Мутант | Унасан тестүүд | Файл |
|---|---|---|---|
| 1 | `if (!student.active())` комментлосон | A3 `register_inactiveStudent_returnsInactiveAndNeverTouchesEmail`, A8 `register_invalidScenario_...[2]` (Failures: 2) | `results/mvn-test-mutant-1.txt` |
| 2 | `saveRegistration(...)` комментлосон | A1 `register_activeStudent_returnsOkSavesAndSendsEmail`, A5 `register_emailSenderThrows_stillReturnsOkAndSaves` (Failures: 2) | `results/mvn-test-mutant-2.txt` |
| 3 | `\|\| courseId == null` устгасан | A6 `register_nullArgument_throwsIllegalArgumentAndNeverTouchesDependencies` (Failures: 1) | `results/mvn-test-mutant-3.txt` |

Мутант 3-ыг A6 шууд барьсан, учир нь A6 нь `studentId`, `courseId` хоёр аргумент тус бүрийн null-ийг тусад нь шалгадаг. Гурван мутант бүгд баригдсан тул эдгээр гурван эвдрэлийн хувьд тестийн цоорхой олдсонгүй. Мутацийн дараа код бүрмөсөн сэргээгдсэн, эцсийн `results/mvn-test.txt` ногоон байна.

## Дүгнэлт

Mock нь хамаарлын дуудалтыг бүртгэж, урьдчилан заасан хариуг буцаадаг объект бөгөөд verify-аар хэдэн удаа, ямар аргументтай дуудагдсаныг шалгаж болно. Гараар бичсэн fake нь жинхэнэ ажилладаг хөнгөн хэрэгжүүлэлт, жишээ нь Map-д өгөгдөл хадгалдаг санах ойн repository юм. Fake нь төлөв хадгалдаг тул үр дүнг шалгахад тохиромжтой ч түүнийг өөрөө бичиж, интерфейс өөрчлөгдөх бүрт засах хэрэгтэй болдог. Mock нь хурдан тохируулагдаж, doThrow-оор алдаа дуурайлгахад ялангуяа ээлтэй. Харин `Student` шиг өгөгдлийн класс, `List`, `String` зэрэг энгийн утга болон стандарт сангийн классыг mock хийх ёсгүй, жинхэнийг нь шууд ашиглана. Мөн тестлэж буй классыг өөрийг нь mock хийж болохгүй, зөвхөн өгөгдлийн сан, и-мэйл шиг гадаад хамаарлыг орлуулна. verify-г хэтрүүлбэл тест хэрэгжүүлэлтийн дотоод дэлгэрэнгүйд наалдаж, зөв ажиллаж байгаа кодыг рефакторлоход ч тест унадаг болно. Тиймээс буцаасан үр дүн болон чухал сайд нөлөө (бүртгэл хадгалагдсан эсэх, и-мэйл явсан эсэх)-г шалгаж, үлдсэн дуудалтыг шаардлагагүй verify хийхгүй байх нь зөв.

## Нэмэлт даалгавар: CI

`.github/workflows/test.yml` нь push бүрд GitHub Actions дээр `mvn test`-ийг JDK 17-оор ажиллуулдаг. Дээрх badge нь сүүлийн ажиллуулалтын үр дүнг харуулна.
