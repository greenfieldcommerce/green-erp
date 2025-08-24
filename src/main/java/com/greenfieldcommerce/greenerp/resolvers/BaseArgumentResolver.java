package com.greenfieldcommerce.greenerp.resolvers;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.MethodParameter;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.server.ResponseStatusException;

import com.greenfieldcommerce.greenerp.annotations.ValidatedId;
import com.greenfieldcommerce.greenerp.exceptions.EntityNotFoundException;

import jakarta.servlet.http.HttpServletRequest;

public abstract class BaseArgumentResolver<T, ID> implements HandlerMethodArgumentResolver
{
	private final Class<ID> entityIdClass;
	private final CrudRepository<T, ID> repository;

	protected BaseArgumentResolver(final Class<ID> entityIdClass, final CrudRepository<T, ID> repository)
	{
		this.entityIdClass = entityIdClass;
		this.repository = repository;
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter)
	{
		return parameter.hasParameterAnnotation(ValidatedId.class) && parameter.getParameterType().equals(entityIdClass)
			&& Arrays.stream(parameter.getParameterAnnotations()).filter(ValidatedId.class::isInstance).map(ValidatedId.class::cast).anyMatch(a -> a.value().equals(getIdParameterName()));
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory)
	{
		final HttpServletRequest nativeRequest = webRequest.getNativeRequest(HttpServletRequest.class);
		if (nativeRequest == null)
			return null;

		final ID id = extract(nativeRequest.getRequestURI(), getResourceName(), getIdRegex(), getIdParser()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid resource id"));
		if (repository.existsById(id))
		{
			return id;
		}

		throw new EntityNotFoundException("ENTITY_NOT_FOUND", String.format("%s with id '%s' not found", getDescription(), id));
	}

	protected String getIdRegex()
	{
		return "\\d+";
	}

	protected abstract String getIdParameterName();

	protected abstract String getResourceName();

	protected abstract String getDescription();

	protected abstract Function<String, ID> getIdParser();

	private Pattern buildPattern(String resourceName, String idRegex)
	{
		String regex = String.format("(?:^|/)%s/(?<id>%s)(?=/|$)", Pattern.quote(resourceName), idRegex);
		return Pattern.compile(regex);
	}

	private Optional<ID> extract(String path, String resourceName, String idRegex, Function<String, ID> parser)
	{
		Pattern pattern = buildPattern(resourceName, idRegex);
		Matcher matcher = pattern.matcher(path);
		if (matcher.find())
		{
			return Optional.of(parser.apply(matcher.group("id")));
		}
		return Optional.empty();
	}
}
