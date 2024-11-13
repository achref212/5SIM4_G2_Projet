package tn.esprit.spring;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tn.esprit.spring.entities.*;
import tn.esprit.spring.repositories.*;
import tn.esprit.spring.services.SkierServicesImpl;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SkierServiceImplTest {

    @InjectMocks
    private SkierServicesImpl skierService;

    @Mock
    private ISkierRepository skierRepository;

    @Mock
    private IPisteRepository pisteRepository;

    @Mock
    private ICourseRepository courseRepository;

    @Mock
    private ISubscriptionRepository subscriptionRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRetrieveAllSkiers() {
        when(skierRepository.findAll()).thenReturn(List.of(new Skier(), new Skier()));
        List<Skier> skiers = skierService.retrieveAllSkiers();
        assertEquals(2, skiers.size());
        verify(skierRepository, times(1)).findAll();
    }

    @Test
    void testAddSkier() {
        Skier skier = new Skier();
        Subscription subscription = new Subscription();
        subscription.setTypeSub(TypeSubscription.ANNUAL);  // Définir le type d'abonnement
        subscription.setStartDate(LocalDate.now());  // Définir une date de début valide
        skier.setSubscription(subscription);

        when(skierRepository.save(skier)).thenReturn(skier);

        Skier savedSkier = skierService.addSkier(skier);
        assertNotNull(savedSkier);
        verify(skierRepository, times(1)).save(skier);
    }


    @Test
    void testAddSkierAndAssignToCourse() {
        Skier skier = new Skier();
        skier.setRegistrations(new HashSet<>());  // Initialiser les registrations comme un ensemble vide
        when(skierRepository.save(skier)).thenReturn(skier);
        when(courseRepository.getById(1L)).thenReturn(new Course());

        Skier result = skierService.addSkierAndAssignToCourse(skier, 1L);
        assertNotNull(result);
        verify(skierRepository, times(1)).save(skier);
        verify(courseRepository, times(1)).getById(1L);
    }

    @Test
    void testAssignSkierToSubscription() {
        Skier skier = new Skier();
        Subscription subscription = new Subscription();
        when(skierRepository.findById(1L)).thenReturn(Optional.of(skier));
        when(subscriptionRepository.findById(2L)).thenReturn(Optional.of(subscription));
        when(skierRepository.save(skier)).thenReturn(skier);

        Skier result = skierService.assignSkierToSubscription(1L, 2L);
        assertNotNull(result);
        assertEquals(subscription, result.getSubscription());
        verify(skierRepository, times(1)).save(skier);
    }



    @Test
    void testRemoveSkier() {
        skierService.removeSkier(1L);
        verify(skierRepository, times(1)).deleteById(1L);
    }

    @Test
    void testRetrieveSkier() {
        Skier skier = new Skier();
        when(skierRepository.findById(1L)).thenReturn(Optional.of(skier));

        Skier result = skierService.retrieveSkier(1L);
        assertNotNull(result);
        verify(skierRepository, times(1)).findById(1L);
    }

    @Test
    void testAssignSkierToPiste() {
        Skier skier = new Skier();
        Piste piste = new Piste();
        when(skierRepository.findById(1L)).thenReturn(Optional.of(skier));
        when(pisteRepository.findById(2L)).thenReturn(Optional.of(piste));
        when(skierRepository.save(skier)).thenReturn(skier);

        Skier result = skierService.assignSkierToPiste(1L, 2L);
        assertNotNull(result);
        verify(skierRepository, times(1)).save(skier);
    }

    @Test
    void testRetrieveSkiersBySubscriptionType() {
        when(skierRepository.findBySubscription_TypeSub(TypeSubscription.ANNUAL))
                .thenReturn(List.of(new Skier()));

        List<Skier> skiers = skierService.retrieveSkiersBySubscriptionType(TypeSubscription.ANNUAL);
        assertEquals(1, skiers.size());
        verify(skierRepository, times(1)).findBySubscription_TypeSub(TypeSubscription.ANNUAL);
    }
}
