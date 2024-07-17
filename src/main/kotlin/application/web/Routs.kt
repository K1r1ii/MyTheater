package application.web

import application.config.AppConfig
import application.data.PerformsStorage
import application.data.ShowsStorage
import application.data.TicketsStorage
import application.data.UsersStorage
import application.web.handlers.HomeHandler
import application.web.handlers.performHandlers.AddPerformsHandler
import application.web.handlers.performHandlers.ConfirmDeletePerformsHandler
import application.web.handlers.performHandlers.DeletePerformsHandler
import application.web.handlers.performHandlers.NewPerformsHandler
import application.web.handlers.performHandlers.PerformsHandler
import application.web.handlers.showHandlers.AddShowHandler
import application.web.handlers.showHandlers.ConfirmDeleteShowHandler
import application.web.handlers.showHandlers.DeleteShowHandler
import application.web.handlers.showHandlers.NewShowHandler
import application.web.handlers.showHandlers.ShowHandler
import application.web.handlers.ticketHandlers.AddTicketHandler
import application.web.handlers.ticketHandlers.BuyTicketHandler
import application.web.handlers.ticketHandlers.ConfirmBuyTicketHandler
import application.web.handlers.ticketHandlers.NewTicketHandler
import application.web.handlers.ticketHandlers.PerformHandler
import application.web.handlers.userHandlers.AddEditUserHandler
import application.web.handlers.userHandlers.AuthUserHandler
import application.web.handlers.userHandlers.ConfirmDeleteUserHandler
import application.web.handlers.userHandlers.DeleteUserHandler
import application.web.handlers.userHandlers.EditUserHandler
import application.web.handlers.userHandlers.LogoutHandler
import application.web.handlers.userHandlers.RegisterUserHandler
import application.web.handlers.userHandlers.SignInHandler
import application.web.handlers.userHandlers.SignUpHandler
import application.web.handlers.userHandlers.UserTicketsHandler
import application.web.handlers.userHandlers.UsersHandler
import org.http4k.core.Method
import org.http4k.lens.RequestContextLens
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.template.TemplateRenderer

fun router(
    key: RequestContextLens<String>,
    tickets: TicketsStorage,
    shows: ShowsStorage,
    performs: PerformsStorage,
    users: UsersStorage,
    renderer: TemplateRenderer,
    config: AppConfig,
) = routes(
    "/" bind Method.GET to HomeHandler(renderer, key, users),
    "/signUp" bind Method.GET to SignUpHandler(renderer, key, users),
    "/signUp" bind Method.POST to RegisterUserHandler(renderer, users, key, config),
    "/signIn" bind Method.GET to SignInHandler(renderer, key, users),
    "/signIn" bind Method.POST to AuthUserHandler(renderer, users, key, config),
    "/logout" bind Method.GET to LogoutHandler(),
    "/users" bind Method.GET to UsersHandler(renderer, users, key),
    "/userTickets" bind Method.GET to UserTicketsHandler(renderer, tickets, key, users),
    "/users/edit" bind Method.GET to EditUserHandler(renderer, key, users),
    "/users/edit" bind Method.POST to AddEditUserHandler(renderer, key, users),
    "/users/delete" bind Method.GET to ConfirmDeleteUserHandler(renderer, key, users),
    "/users/delete" bind Method.POST to DeleteUserHandler(renderer, key, users),
    "/performances" bind Method.GET to PerformsHandler(renderer, performs, key, users),
    "/performances/new" bind Method.GET to NewPerformsHandler(renderer, performs, key, users),
    "/performances/new" bind Method.POST to AddPerformsHandler(renderer, performs, key, users),
    "/performances/edit" bind Method.GET to NewPerformsHandler(renderer, performs, key, users),
    "/performances/edit" bind Method.POST to AddPerformsHandler(renderer, performs, key, users),
    "/performances/delete" bind Method.GET to ConfirmDeletePerformsHandler(renderer, performs, key, users),
    "/performances/delete" bind Method.POST to DeletePerformsHandler(renderer, performs, shows, tickets, key, users),
    "/performances/{performId}" bind Method.GET to ShowHandler(renderer, performs, shows, key, users),
    "/performances/{performId}/new" bind Method.GET to NewShowHandler(renderer, shows, key, users),
    "/performances/{performId}/new" bind Method.POST to AddShowHandler(renderer, performs, shows, key, users),
    "/performances/{performId}/edit" bind Method.GET to NewShowHandler(renderer, shows, key, users),
    "/performances/{performId}/edit" bind Method.POST to AddShowHandler(renderer, performs, shows, key, users),
    "/performances/{performId}/delete" bind Method.GET to ConfirmDeleteShowHandler(renderer, shows, key, users),
    "/performances/{performId}/delete" bind Method.POST to DeleteShowHandler(renderer, performs, shows, tickets, key, users),
    "/performances/{performId}/{showId}" bind Method.GET to PerformHandler(renderer, performs, shows, tickets, key, users),
    "/performances/{performId}/{showId}/new" bind Method.GET to NewTicketHandler(renderer, tickets, key, users),
    "/performances/{performId}/{showId}/new" bind Method.POST to AddTicketHandler(renderer, performs, shows, tickets, key, users),
    "/performances/{performId}/{showId}/edit" bind Method.GET to NewTicketHandler(renderer, tickets, key, users),
    "/performances/{performId}/{showId}/edit" bind Method.POST to AddTicketHandler(renderer, performs, shows, tickets, key, users),
    "/performances/{performId}/{showId}/buy" bind Method.GET to ConfirmBuyTicketHandler(renderer, tickets, key, users),
    "/performances/{performId}/{showId}/buy" bind Method.POST to BuyTicketHandler(renderer, performs, shows, tickets, key, users),
)
