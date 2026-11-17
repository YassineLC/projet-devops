export type Livre = {
  id: number
  isbn: string | null
  titre: string
  auteurs: string | null
  annee: number | null
  couvertureUrl: string | null
  exemplairesTotal: number
  exemplairesDisponibles: number
}

export type Statut = 'EN_COURS' | 'EN_RETARD' | 'RENDU'

export type Emprunt = {
  id: number
  livreId: number
  titreLivre: string
  emprunteur: string
  dateEmprunt: string
  dateRetourPrevue: string
  dateRetour: string | null
  statut: Statut
}

async function appel<T>(url: string, init?: RequestInit): Promise<T> {
  const reponse = await fetch(url, {
    ...init,
    headers: { 'Content-Type': 'application/json', ...init?.headers },
  })
  if (!reponse.ok) {
    const corps = await reponse.json().catch(() => null)
    throw new Error(corps?.erreur ?? `Le serveur a répondu ${reponse.status}`)
  }
  return reponse.json() as Promise<T>
}

export const api = {
  livres: (q: string) => appel<Livre[]>(`/api/livres?q=${encodeURIComponent(q)}`),
  emprunter: (livreId: number, emprunteur: string) =>
    appel<Emprunt>('/api/emprunts', { method: 'POST', body: JSON.stringify({ livreId, emprunteur }) }),
  emprunts: (emprunteur: string) =>
    appel<Emprunt[]>(`/api/emprunts?emprunteur=${encodeURIComponent(emprunteur)}`),
  rendre: (id: number) => appel<Emprunt>(`/api/emprunts/${id}/retour`, { method: 'POST' }),
}

export function dateFr(iso: string) {
  return new Date(iso).toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' })
}
