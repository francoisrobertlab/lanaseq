package ca.qc.ircm.lanaseq.files.web;

import static ca.qc.ircm.lanaseq.text.Strings.comparable;

import com.vaadin.flow.function.SerializablePredicate;
import java.io.Serial;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Supplier;
import org.springframework.data.domain.Range;

/**
 * Filter for public files.
 */
public class PublicFileFilter implements SerializablePredicate<PublicFile> {

  @Serial
  private static final long serialVersionUID = 5824699027530674789L;
  public String filenameContains;
  public Range<LocalDate> expiryDateRange;
  public String sampleNameContains;
  public String ownerContains;

  @Override
  public boolean test(PublicFile publicFile) {
    boolean test = true;
    if (filenameContains != null) {
      test &= comparable(publicFile.getFilename()).contains(comparable(filenameContains));
    }
    if (expiryDateRange != null) {
      test &= expiryDateRange.contains(publicFile.getExpiryDate(), Comparator.naturalOrder());
    }
    if (sampleNameContains != null) {
      test &= comparable(publicFile.getSampleName()).contains(comparable(sampleNameContains));
    }
    if (ownerContains != null) {
      test &= comparable(replaceNull(() -> publicFile.getOwner().getEmail())).contains(
          comparable(ownerContains)) || comparable(
          replaceNull(() -> publicFile.getOwner().getName())).contains(comparable(ownerContains));
    }
    return test;
  }

  private String replaceNull(Supplier<String> supplier) {
    try {
      return Objects.toString(supplier.get(), "");
    } catch (NullPointerException e) {
      return "";
    }
  }
}
