resource "aws_acm_certificate" "alb" {
  domain_name       = var.subdomain != "" ? "${var.subdomain}.${var.domain_name}" : var.domain_name
  validation_method = "DNS"

  subject_alternative_names = var.subdomain != "" ? [var.domain_name] : []

  lifecycle {
    create_before_destroy = true
  }

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}-alb-certificate"
    }
  )
}

resource "aws_route53_record" "certificate_validation" {
  for_each = {
    for dvo in aws_acm_certificate.alb.domain_validation_options : dvo.domain_name => {
      name   = dvo.resource_record_name
      record = dvo.resource_record_value
      type   = dvo.resource_record_type
    }
  }

  allow_overwrite = true
  name            = each.value.name
  records         = [each.value.record]
  ttl             = var.acm_certificate_validation_ttl
  type            = each.value.type
  zone_id         = aws_route53_zone.main.zone_id
}

resource "aws_acm_certificate_validation" "alb" {
  certificate_arn         = aws_acm_certificate.alb.arn
  validation_record_fqdns = [for record in aws_route53_record.certificate_validation : record.fqdn]

  timeouts {
    create = var.acm_certificate_validation_timeout
  }
}
